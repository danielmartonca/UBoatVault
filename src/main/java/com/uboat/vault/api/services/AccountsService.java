package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestAccount;
import com.uboat.vault.api.model.http.new_requests.RequestAccountDetails;
import com.uboat.vault.api.model.http.new_requests.RequestCreditCard;
import com.uboat.vault.api.model.http.new_requests.RequestMissingAccountInformation;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.model.persistence.sailing.sailor.BoatImage;
import com.uboat.vault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AccountsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final EntityService entityService;
    private final JwtService jwtService;

    private final AccountsRepository accountsRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final CreditCardsRepository creditCardsRepository;
    private final ActiveSailorsRepository activeSailorsRepository;
    private final BoatsRepository boatsRepository;
    private final BoatImagesRepository boatImagesRepository;

    @Autowired
    public AccountsService(EntityService entityService, JwtService jwtService, @Lazy BoatImagesRepository boatImagesRepository, AccountsRepository accountsRepository, AccountDetailsRepository accountDetailsRepository, ImagesRepository imagesRepository, CreditCardsRepository creditCardsRepository, ActiveSailorsRepository activeSailorsRepository, BoatsRepository boatsRepository) {
        this.entityService = entityService;
        this.jwtService = jwtService;
        this.accountsRepository = accountsRepository;
        this.accountDetailsRepository = accountDetailsRepository;
        this.creditCardsRepository = creditCardsRepository;
        this.activeSailorsRepository = activeSailorsRepository;
        this.boatsRepository = boatsRepository;
        this.boatImagesRepository = boatImagesRepository;
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
            accountDetailsRepository.save(accountDetails);

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
    public Boolean addCreditCard(Account requestAccount, CreditCard creditCard) {
        var foundAccount = entityService.findAccountByCredentials(Credentials.fromAccount(requestAccount));
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }

        try {
            if (creditCard.isExpired()) {
                log.info("Token and account match but credit card is expired.");
                return false;
            }
        } catch (ParseException e) {
            log.info("Invalid format for credit card expiration date: '" + creditCard.getExpirationDate() + "'", e);
            return false;
        }

        CreditCard card = creditCardsRepository.findFirstByNumberAndCvc(creditCard.getNumber(), creditCard.getCvc());
        if (card != null) {
            log.warn("Credit card already exists in the database.");
            return true;
        }
        log.info("Token and account credentials match. Adding new credit card to account.");

        creditCard.setAccount(foundAccount);
        Set<CreditCard> userCreditCards = foundAccount.getCreditCards();
        if (userCreditCards == null) {
            log.info("User did not have any credit cards setup.");
            userCreditCards = new HashSet<>();
            foundAccount.setCreditCards(userCreditCards);
        }
        userCreditCards.add(creditCard);

        accountsRepository.save(foundAccount);
        log.info("Saved new credit card in the database.");
        return true;
    }

    @Transactional
    public Boolean deleteCreditCard(Account requestAccount, CreditCard creditCard) {
        var foundAccount = entityService.findAccountByCredentials(Credentials.fromAccount(requestAccount));
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }
        log.info("Token and account are matching.");

        if (foundAccount.getCreditCards() == null || foundAccount.getCreditCards().isEmpty()) {
            log.warn("Account does not have any credit cards.");
            return false;
        }

        for (var card : foundAccount.getCreditCards())
            if (card.equals(creditCard)) {
                creditCardsRepository.deleteById(card.getId());
                log.info("Found matching request credit card to the account. Deleting entry from database.");
                return true;
            }

        log.warn("Couldn't find the request credit card belonging to the given account.");
        return false;
    }


    /**
     * Used by sailors to get and initialise their boat account
     */
    @Transactional
    public Boat getBoat(Account requestAccount) {
        var foundActiveSailor = entityService.findActiveSailorByCredentials(requestAccount);

        if (foundActiveSailor == null) {
            log.warn("No account was retrieved. Aborting");
            return null;
        }

        if (foundActiveSailor.getBoat() == null) {
            log.warn("Sailor does not have any boat set. Creating empty boat now.");
            var boat = new Boat();
            boat.setSailor(foundActiveSailor);
            foundActiveSailor.setBoat(boat);
            activeSailorsRepository.save(foundActiveSailor);
            return boat;
        }

        log.info("Found boat details for sailor with id " + foundActiveSailor.getAccountId());
        return foundActiveSailor.getBoat();
    }

    /**
     * Used by clients to retrieve informations about their journey.
     */
    @Transactional
    public Boat getBoat(String sailorId) {
        var foundActiveSailor = entityService.findActiveSailorBySailorId(sailorId);

        if (foundActiveSailor == null)
            return null;

        if (foundActiveSailor.getBoat() == null) {
            log.warn("Sailor does not have any boat set. Creating empty boat now.");
            return null;
        }

        log.info("Found boat details for sailor with id " + foundActiveSailor.getAccountId());
        return foundActiveSailor.getBoat();
    }

    @Transactional
    public Boat updateBoat(Account requestAccount, Boat boat) {
        var foundActiveSailor = entityService.findActiveSailorByCredentials(requestAccount);

        if (foundActiveSailor == null) {
            log.warn("No active sailor was retrieved. Aborting");
            return null;
        }

        updateDatabaseBoat(foundActiveSailor.getBoat(), boat);

        return foundActiveSailor.getBoat();
    }

    @Transactional
    public void updateDatabaseBoat(Boat foundBoat, Boat requestBoat) {
        boolean hasChanged = false;

        if (foundBoat.getType() == null && requestBoat.getType() != null) {
            foundBoat.setType(requestBoat.getType());
            hasChanged = true;
        } else if (foundBoat.getType() != null && !requestBoat.getType().isEmpty())
            if (!foundBoat.getType().equals(requestBoat.getType())) {
                log.info("Boat type was '" + requestBoat.getType() + "'. Updated it to '" + foundBoat.getType() + "'.");
                foundBoat.setType(requestBoat.getType());
                hasChanged = true;
            }

        if (foundBoat.getModel() == null && requestBoat.getModel() != null) {
            foundBoat.setModel(requestBoat.getModel());
            hasChanged = true;
        } else if (foundBoat.getModel() != null && !requestBoat.getModel().isEmpty())
            if (!foundBoat.getModel().equals(requestBoat.getModel())) {
                log.info("Boat model was '" + requestBoat.getModel() + "'. Updated it to '" + foundBoat.getModel() + "'.");
                foundBoat.setModel(requestBoat.getModel());
                hasChanged = true;
            }

        if (foundBoat.getLicenseNumber() == null && requestBoat.getLicenseNumber() != null) {
            foundBoat.setLicenseNumber(requestBoat.getLicenseNumber());
            hasChanged = true;
        } else if (foundBoat.getLicenseNumber() != null && !requestBoat.getLicenseNumber().isEmpty())
            if (!foundBoat.getLicenseNumber().equals(requestBoat.getLicenseNumber())) {
                log.info("Boat license number was '" + requestBoat.getLicenseNumber() + "'. Updated it to '" + foundBoat.getLicenseNumber() + "'.");
                foundBoat.setLicenseNumber(requestBoat.getLicenseNumber());
                hasChanged = true;
            }

        if (foundBoat.getColor() == null && requestBoat.getColor() != null) {
            foundBoat.setColor(requestBoat.getColor());
            hasChanged = true;
        } else if (foundBoat.getColor() != null && !requestBoat.getColor().isEmpty())
            if (!foundBoat.getColor().equals(requestBoat.getColor())) {
                log.info("Boat color was '" + requestBoat.getColor() + "'. Updated it to '" + foundBoat.getColor() + "'.");
                foundBoat.setColor(requestBoat.getColor());
                hasChanged = true;
            }

        if (foundBoat.getAverageSpeedMeasureUnit() == null && requestBoat.getAverageSpeedMeasureUnit() != null) {
            foundBoat.setAverageSpeedMeasureUnit(requestBoat.getAverageSpeedMeasureUnit());
            hasChanged = true;
        } else if (foundBoat.getAverageSpeedMeasureUnit() != null && !requestBoat.getAverageSpeedMeasureUnit().isEmpty())
            if (!foundBoat.getAverageSpeedMeasureUnit().equals(requestBoat.getAverageSpeedMeasureUnit())) {
                log.info("Boat AverageSpeedMeasureUnit was '" + requestBoat.getAverageSpeedMeasureUnit() + "'. Updated it to '" + foundBoat.getAverageSpeedMeasureUnit() + "'.");
                foundBoat.setAverageSpeedMeasureUnit(requestBoat.getAverageSpeedMeasureUnit());
                hasChanged = true;
            }

        if (foundBoat.getAverageSpeed() == 0 && requestBoat.getAverageSpeed() != 0) {
            foundBoat.setAverageSpeed(requestBoat.getAverageSpeed());
            hasChanged = true;
        } else if (foundBoat.getAverageSpeed() != 0 && requestBoat.getAverageSpeed() != 0)
            if (!(foundBoat.getAverageSpeed() == requestBoat.getAverageSpeed())) {
                log.info("Boat average speed was '" + requestBoat.getAverageSpeed() + "'. Updated it to '" + foundBoat.getAverageSpeed() + "'.");
                foundBoat.setAverageSpeed(requestBoat.getAverageSpeed());
                hasChanged = true;
            }

        if ((foundBoat.getBoatImages() == null || foundBoat.getBoatImages().isEmpty()) &&
                (requestBoat.getBoatImages() != null && !requestBoat.getBoatImages().isEmpty())) {
            Set<BoatImage> boatImages = new HashSet<>();
            for (var boatImage : requestBoat.getBoatImages())
                boatImages.add(new BoatImage(boatImage.getBytes(), foundBoat));
            foundBoat.setBoatImages(boatImages);
            boatImagesRepository.saveAll(boatImages);
            log.info("Boat had no images. Added all images from the request to the boat.");
        } else if (foundBoat.getBoatImages() != null && requestBoat.getBoatImages() != null)
            if (requestBoat.getBoatImages().size() != 0) {

                Set<BoatImage> newBoatImages = new HashSet<>();
                for (var requestBoatImage : requestBoat.getBoatImages())
                    for (var boatImage : foundBoat.getBoatImages())
                        if (!boatImage.equals(requestBoatImage))
                            newBoatImages.add(new BoatImage(requestBoatImage.getBytes(), foundBoat));
                if (!newBoatImages.isEmpty()) {
                    log.info("Found new boat images. Updating boat images.");
                    foundBoat.getBoatImages().addAll(newBoatImages);
                    boatImagesRepository.saveAll(newBoatImages);
                    hasChanged = true;
                }
            }
        if (hasChanged) {
            boatsRepository.save(foundBoat);
            log.info("Updated boat.");
        } else log.info("Boat is identical.");
    }

    public String getSailorName(String sailorId) {
        var foundAccount = entityService.findSailorAccountById(sailorId);
        if (foundAccount == null) return null;

        var accountDetails = foundAccount.getAccountDetails();
        if (accountDetails == null || accountDetails.getFullName() == null || accountDetails.getFullName().isEmpty()) {
            log.warn("Account details is null. Sailor does not have a username set yet. Returning username from Account.");
            return foundAccount.getUsername();
        }

        log.info("Found username for sailor id " + sailorId + ": " + accountDetails.getFullName());
        return accountDetails.getFullName();
    }
}

