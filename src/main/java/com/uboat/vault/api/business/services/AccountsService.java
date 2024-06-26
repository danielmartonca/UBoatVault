package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.account.CreditCard;
import com.uboat.vault.api.model.dto.*;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountsService {
    private final EntityService entityService;
    private final JwtService jwtService;
    private final AccountsRepository accountsRepository;
    private final SailorsRepository sailorsRepository;


    /**
     * This method returns full account information for the account in the request that is missing phone number/username.
     * If the account doesn't exist or the JWT is invalid/not matching the account information, a client error message is returned.
     */
    public UBoatDTO getMissingAccountInformation(AccountDTO accountDTO, String authorizationHeader) {
        try {
            var foundAccount = entityService.findAccountByCredentials(Credentials.fromRequest(accountDTO));

            if (foundAccount == null) {
                log.warn("Failed to find any account matching request account.");
                return new UBoatDTO(UBoatStatus.ACCOUNT_NOT_FOUND);
            }

            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);

            if (foundAccount.getUsername().equals(jwtData.username()) || foundAccount.getPhone().getNumber().equals(jwtData.phoneNumber()))
                return new UBoatDTO(UBoatStatus.MISSING_ACCOUNT_INFORMATION_RETRIEVED, new MissingAccountInformationDTO(foundAccount));

            return new UBoatDTO(UBoatStatus.CREDENTIALS_NOT_MATCHING_JWT);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving missing account information.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * This method returns extra details about the account based on the JWT passed in the request authorization header.
     */
    public UBoatDTO getAccountDetails(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            log.debug("JWT data extracted.");
            var account = entityService.findAccountByJwtData(jwtData);

            //accountDetails can't be null due to its initialization during registration
            var accountDetails = new AccountDetailsDTO(account);
            log.info("Account details retrieved successfully.");
            return new UBoatDTO(UBoatStatus.ACCOUNT_DETAILS_RETRIEVED, accountDetails);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving extra account details.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO updateAccountDetails(String authorizationHeader, AccountDetailsDTO accountDetailsDTO) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //accountDetails can't be null due to its initialization during registration
            var accountDetails = account.getAccountDetails();
            accountDetails.update(accountDetailsDTO);
            accountsRepository.save(account);

            log.info("Account details updated successfully.");
            return new UBoatDTO(UBoatStatus.ACCOUNT_DETAILS_UPDATED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while updating account details.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    public UBoatDTO getCreditCards(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //cards can't be null due to its initialization during registration
            var creditCards = account.getCreditCards().stream().map(CreditCardDTO::new).collect(Collectors.toSet());

            log.info("Returning " + creditCards.size() + " credit cards.");
            return new UBoatDTO(UBoatStatus.CREDIT_CARDS_RETRIEVED, creditCards);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving credit cards.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatDTO addCreditCard(String authorizationHeader, CreditCardDTO newCreditCard) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            var validationStatus = CreditCard.validate(newCreditCard);
            return switch (validationStatus) {
                case EXPIRED -> new UBoatDTO(UBoatStatus.CREDIT_CARD_EXPIRED, false);

                case VALID -> {
                    var accountCards = account.getCreditCards();

                    //if there is already a card with the given owner full name and number
                    if (accountCards.stream().anyMatch(creditCard -> newCreditCard.getNumber().equals(creditCard.getNumber()) && newCreditCard.getOwnerFullName().equals(creditCard.getOwnerFullName()))) {
                        log.warn("There is already an credit card with number '{}' and Owner Name '{}' for the account.", newCreditCard.getNumber(), newCreditCard.getNumber());
                        yield new UBoatDTO(UBoatStatus.CREDIT_CARD_DUPLICATE, true);
                    }

                    var newAccountCreditCard = new CreditCard(account, newCreditCard);
                    accountCards.add(newAccountCreditCard);

                    accountsRepository.save(account);
                    log.info("Added new credit card to the account");
                    yield new UBoatDTO(UBoatStatus.CREDIT_CARD_ADDED, true);
                }
            };
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while adding new credit card.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatDTO deleteCreditCard(String authorizationHeader, CreditCardDTO creditCard) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //cant be null because it is created during registration
            var creditCards = account.getCreditCards();

            for (var card : creditCards)
                if (card.equalsRequestCard(creditCard)) {

                    creditCards.remove(card);
                    accountsRepository.save(account);

                    log.info("Credit card was deleted from the account.");
                    return new UBoatDTO(UBoatStatus.CREDIT_CARD_DELETED, true);
                }

            if (creditCards.isEmpty())
                log.warn("Account does not have any credit cards set. Cannot delete card from the request.");
            else log.warn("Couldn't find the request credit card belonging to the given account.");

            return new UBoatDTO(UBoatStatus.CREDIT_CARD_NOT_FOUND, false);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while deleting credit card.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    /**
     * Used by sailors to get and retrieve their boat account
     */
    @Transactional
    public UBoatDTO getMyBoat(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //cant be null because the API is accessible only to sailors which create the sailor upon registration
            var sailor = sailorsRepository.findFirstByAccountId(account.getId());

            //cant be null because the boat is created during sailor registration
            var boat = sailor.getBoat();

            log.info("Retrieved boat for the sailor.");
            return new UBoatDTO(UBoatStatus.BOAT_RETRIEVED, boat);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving my boat", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatDTO updateMyBoat(String authorizationHeader, BoatDTO newBoatDetails) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //cant be null because the API is accessible only to sailors which create the sailor upon registration
            var sailor = sailorsRepository.findFirstByAccountId(account.getId());

            //cant be null because the boat is created during sailor registration
            var boat = sailor.getBoat();

            boat.update(newBoatDetails);

            log.info("Updated the boat of the sailor.");
            return new UBoatDTO(UBoatStatus.BOAT_UPDATED, boat);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while updating my boat", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }


    /**
     * Used by clients to retrieve information about their journey.
     */
    @Transactional
    public UBoatDTO getSailorBoat(String sailorId) {
        try {
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null) return new UBoatDTO(UBoatStatus.SAILOR_NOT_FOUND);
            return new UBoatDTO(UBoatStatus.SAILOR_BOAT_RETRIEVED, new BoatDTO(sailor.getBoat()));
        } catch (Exception e) {
            log.error("An exception occurred while retrieving journey boat.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }
}

