package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.model.http.*;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.repositories.SailorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;


@Service
public class AccountsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final EntityService entityService;
    private final JwtService jwtService;
    private final AccountsRepository accountsRepository;
    private final SailorsRepository sailorsRepository;

    @Autowired
    public AccountsService(EntityService entityService, JwtService jwtService, AccountsRepository accountsRepository, SailorsRepository sailorsRepository) {
        this.entityService = entityService;
        this.jwtService = jwtService;
        this.accountsRepository = accountsRepository;
        this.sailorsRepository = sailorsRepository;
    }


    /**
     * This method returns full account information for the account in the request that is missing phone number/username.
     * If the account doesn't exist or the JWT is invalid/not matching the account information, a client error message is returned.
     */
    public UBoatResponse getMissingAccountInformation(RequestAccount requestAccount, String authorizationHeader) {
        try {
            var foundAccount = entityService.findAccountByCredentials(Credentials.fromRequest(requestAccount));

            if (foundAccount == null) {
                log.warn("Failed to find any account matching request account.");
                return new UBoatResponse(UBoatStatus.ACCOUNT_NOT_FOUND);
            }

            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);

            if (foundAccount.getUsername().equals(jwtData.username()) || foundAccount.getPhoneNumber().getPhoneNumber().equals(jwtData.phoneNumber()))
                return new UBoatResponse(UBoatStatus.MISSING_ACCOUNT_INFORMATION_RETRIEVED, new RequestMissingAccountInformation(foundAccount));

            return new UBoatResponse(UBoatStatus.CREDENTIALS_NOT_MATCHING_JWT);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving missing account information.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method returns extra details about the account based on the JWT passed in the request authorization header.
     */
    public UBoatResponse getAccountDetails(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());
            log.debug("JWT data extracted.");

            //accountDetails can't be null due to its initialization during registration
            var accountDetails = new RequestAccountDetails(account.getAccountDetails());
            log.info("Account details retrieved successfully.");
            return new UBoatResponse(UBoatStatus.ACCOUNT_DETAILS_RETRIEVED, accountDetails);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving extra account details.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatResponse updateAccountDetails(String authorizationHeader, RequestAccountDetails requestAccountDetails) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //accountDetails can't be null due to its initialization during registration
            var accountDetails = account.getAccountDetails();
            accountDetails.update(requestAccountDetails);
            accountsRepository.save(account);

            log.info("Account details updated successfully.");
            return new UBoatResponse(UBoatStatus.ACCOUNT_DETAILS_UPDATED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while updating account details.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    public UBoatResponse getCreditCards(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //cards can't be null due to its initialization during registration
            var creditCards = account.getCreditCards().stream().map(RequestCreditCard::new).collect(Collectors.toSet());

            log.info("Returning " + creditCards.size() + " credit cards.");
            return new UBoatResponse(UBoatStatus.CREDIT_CARDS_RETRIEVED, creditCards);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving credit cards.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatResponse addCreditCard(String authorizationHeader, RequestCreditCard newCreditCard) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            var validationStatus = CreditCard.validate(newCreditCard);
            return switch (validationStatus) {
                case EXPIRED -> new UBoatResponse(UBoatStatus.CREDIT_CARD_EXPIRED, false);

                case VALID -> {
                    var accountCards = account.getCreditCards();

                    //if there is already a card with the given owner full name and number
                    if (accountCards.stream().anyMatch(creditCard -> newCreditCard.getNumber().equals(creditCard.getNumber()) && newCreditCard.getOwnerFullName().equals(creditCard.getOwnerFullName()))) {
                        log.warn("There is already an credit card with number '{}' and Owner Name '{}' for the account.", newCreditCard.getNumber(), newCreditCard.getNumber());
                        yield new UBoatResponse(UBoatStatus.CREDIT_CARD_DUPLICATE, true);
                    }

                    var newAccountCreditCard = new CreditCard(account, newCreditCard);
                    accountCards.add(newAccountCreditCard);

                    accountsRepository.save(account);
                    log.info("Added new credit card to the account");
                    yield new UBoatResponse(UBoatStatus.CREDIT_CARD_ADDED, true);
                }
            };
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while adding new credit card.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatResponse deleteCreditCard(String authorizationHeader, RequestCreditCard creditCard) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //cant be null because it is created during registration
            var creditCards = account.getCreditCards();

            for (var card : creditCards)
                if (card.equalsRequestCard(creditCard)) {
                    creditCards.remove(card);
                    accountsRepository.save(account);

                    log.info("Credit card was deleted from the account.");
                    return new UBoatResponse(UBoatStatus.CREDIT_CARD_DELETED, true);
                }

            if (creditCards.isEmpty())
                log.warn("Account does not have any credit cards set. Cannot delete card from the request.");
            else log.warn("Couldn't find the request credit card belonging to the given account.");

            return new UBoatResponse(UBoatStatus.CREDIT_CARD_NOT_FOUND, false);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while deleting credit card.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    /**
     * Used by sailors to get and retrieve their boat account
     */
    public UBoatResponse getMyBoat(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //cant be null because the API is accessible only to sailors which create the sailor upon registration
            var sailor = sailorsRepository.findFirstByAccountId(account.getId());

            //cant be null because the boat is created during sailor registration
            var boat = sailor.getBoat();

            log.info("Retrieved boat for the sailor.");
            return new UBoatResponse(UBoatStatus.BOAT_RETRIEVED, boat);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving my boat", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatResponse updateMyBoat(String authorizationHeader, RequestBoat newBoatDetails) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //cant be null because the API is accessible only to sailors which create the sailor upon registration
            var sailor = sailorsRepository.findFirstByAccountId(account.getId());

            //cant be null because the boat is created during sailor registration
            var boat = sailor.getBoat();

            boat.update(newBoatDetails);

            log.info("Updated the boat of the sailor.");
            return new UBoatResponse(UBoatStatus.BOAT_UPDATED, boat);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while updating my boat", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    public UBoatResponse getSailorDetails(String sailorId) {
        try {
            var sailor = entityService.findSailorByAccountId(sailorId);
            if (sailor == null)
                return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);

            var sailorAccountOptional = accountsRepository.findById(Long.valueOf(sailorId));
            if (sailorAccountOptional.isEmpty())
                throw new RuntimeException("Warning: sailor has account id which does not belong to any account");

            var sailorAccount = sailorAccountOptional.get();

            var sailorDetails = RequestSailorDetails.builder()
                    .fullName(sailorAccount.getAccountDetails().getFullName())
                    .phoneNumber(sailorAccount.getPhoneNumber().getPhoneNumber())
                    .build();

            log.info("Retrieved sailor details successfully.");
            return new UBoatResponse(UBoatStatus.SAILOR_DETAILS_RETRIEVED, sailorDetails);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving details about the sailor.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    /**
     * Used by clients to retrieve information about their journey.
     */
    @Transactional
    public UBoatResponse getSailorBoat(String sailorId) {
        try {
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null) return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);
            return new UBoatResponse(UBoatStatus.SAILOR_BOAT_RETRIEVED, new RequestBoat(sailor.getBoat()));
        } catch (Exception e) {
            log.error("An exception occurred while retrieving journey boat.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }
}

