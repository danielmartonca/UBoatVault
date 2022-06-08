package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.AccountDetails;
import com.example.uboatvault.api.model.persistence.account.CreditCard;
import com.example.uboatvault.api.model.persistence.account.Image;
import com.example.uboatvault.api.model.persistence.location.LocationData;
import com.example.uboatvault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Service
public class AccountsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final ImagesService imagesService;

    private final AccountsRepository accountsRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final TokensRepository tokensRepository;
    private final ImagesRepository imagesRepository;
    private final CreditCardsRepository creditCardsRepository;
    private final ActiveSailorsRepository activeSailorsRepository;
    private final LocationDataRepository locationDataRepository;

    @Autowired
    public AccountsService(@Lazy ImagesService imagesService, AccountsRepository accountsRepository, AccountDetailsRepository accountDetailsRepository, TokensRepository tokensRepository, ImagesRepository imagesRepository, CreditCardsRepository creditCardsRepository, ActiveSailorsRepository activeSailorsRepository, LocationDataRepository locationDataRepository) {
        this.imagesService = imagesService;
        this.accountsRepository = accountsRepository;
        this.accountDetailsRepository = accountDetailsRepository;
        this.tokensRepository = tokensRepository;
        this.imagesRepository = imagesRepository;
        this.creditCardsRepository = creditCardsRepository;
        this.activeSailorsRepository = activeSailorsRepository;
        this.locationDataRepository = locationDataRepository;
    }

    private boolean areAccountsMatching(Account requestAccount, Account foundAccount) {
        if (!foundAccount.getPassword().equals(requestAccount.getPassword())) {
            log.warn("Passwords don't match.");
            return false;
        }

        if (!foundAccount.getUsername().equals(requestAccount.getUsername()) && !foundAccount.getPhoneNumber().equals(requestAccount.getPhoneNumber())) {
            log.warn("Password matches but neither username or phone number match.");
            return false;
        }
        return true;
    }

    @Transactional
    void updateDatabaseAccountDetails(AccountDetails foundAccountDetails, AccountDetails requestAccountDetails) {
        boolean hasChanged = false;

        if (foundAccountDetails.getFullName() == null && requestAccountDetails.getFullName() != null) {
            foundAccountDetails.setFullName(requestAccountDetails.getFullName());
            hasChanged = true;
        } else if (foundAccountDetails.getFullName() != null && !requestAccountDetails.getFullName().isEmpty())
            if (!foundAccountDetails.getFullName().equals(requestAccountDetails.getFullName())) {
                log.info("Account details full name was '" + requestAccountDetails.getFullName() + "'. Updated it to '" + foundAccountDetails.getFullName() + "'.");
                foundAccountDetails.setFullName(requestAccountDetails.getFullName());
                hasChanged = true;
            }

        if (foundAccountDetails.getEmail() == null && requestAccountDetails.getEmail() != null) {
            foundAccountDetails.setEmail(requestAccountDetails.getEmail());
            hasChanged = true;
        } else if (foundAccountDetails.getEmail() != null && !requestAccountDetails.getEmail().isEmpty())
            if (!foundAccountDetails.getEmail().equals(requestAccountDetails.getEmail())) {
                log.info("Account details email was '" + requestAccountDetails.getEmail() + "'. Updated it to '" + foundAccountDetails.getEmail() + "'.");
                foundAccountDetails.setEmail(requestAccountDetails.getEmail());
                hasChanged = true;
            }

        if (foundAccountDetails.getImage() == null && requestAccountDetails.getImage() != null) {
            var newImage = new Image(requestAccountDetails.getImage().getBytes());
            newImage.setAccountDetails(foundAccountDetails);
            foundAccountDetails.setImage(newImage);
            imagesRepository.save(newImage);
        } else if (foundAccountDetails.getImage() != null && requestAccountDetails.getImage().getBytes() != null)
            if (requestAccountDetails.getImage().getBytes().length != 0) {
                log.info("Updating profile picture.");
                foundAccountDetails.setImage(null);
                var image = foundAccountDetails.getImage();
                imagesRepository.deleteById(image.getId());
                var newImage = requestAccountDetails.getImage();
                foundAccountDetails.setImage(newImage);
                newImage.setAccountDetails(foundAccountDetails);
                imagesRepository.save(newImage);
            }

        if (hasChanged) {
            accountDetailsRepository.save(foundAccountDetails);
            imagesRepository.deleteAllUnreferencedImages();
            log.info("Updated database account details.");
        } else log.info("Account details were identical.");
    }

    public Account getAccountByTokenAndCredentials(String token, Account requestAccount) {
        Account foundAccount;

        var foundToken = tokensRepository.findFirstByTokenValue(token);
        if (foundToken == null) {
            log.warn("Token not existing in the database.");
            return null;
        }

        foundAccount = foundToken.getAccount();

        if (!areAccountsMatching(requestAccount, foundAccount)) {
            log.warn("Account in request doesn't match with the account found by token in the database.");
            return null;
        }

        log.info("Credentials are ok. Account retrieved from database is sent back to the user.");
        if (foundAccount.getAccountDetails() != null && foundAccount.getAccountDetails().getImage() != null)
            foundAccount.getAccountDetails().setImage(null);    //don't send the profile picture after every login request
        return foundAccount;
    }

    @Transactional
    public AccountDetails getAccountDetails(String token, Account requestAccount) {
        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }
        var accountDetails = foundAccount.getAccountDetails();
        if (accountDetails == null) {
            log.warn("Account details is null. User hasn't setup any account details.");
            accountDetails = new AccountDetails();
            foundAccount.setAccountDetails(accountDetails);
            accountsRepository.save(foundAccount);
        }
        var img = imagesRepository.findByAccountDetailsId(accountDetails.getId());
        if (img == null) {
            log.warn("Account details image is null. Setting up default profile picture.");
            var imageBytes = imagesService.getDefaultProfilePicture();
            var image = new Image(imageBytes);

            return new AccountDetails(accountDetails.getFullName(), accountDetails.getEmail(), image);
        } else {
            accountDetails.setImage(img);
            accountDetailsRepository.save(accountDetails);
        }

        log.info("Retrieved account details successfully.");
        return accountDetails;
    }

    @Transactional
    public AccountDetails updateAccountDetails(String token, Account requestAccount) {
        var requestAccountDetails = requestAccount.getAccountDetails();
        if (requestAccountDetails == null) {
            log.warn("Request account details is null.");
            return null;
        }

        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }

        var foundAccountDetails = foundAccount.getAccountDetails();
        if (foundAccountDetails == null) {
            log.warn("Account details is null. User hasn't setup any account details.");
            return getAccountDetails(token, requestAccount);
        }
        log.info("Retrieved account details successfully.");

        updateDatabaseAccountDetails(foundAccountDetails, requestAccountDetails);

        return foundAccountDetails;
    }

    public Set<CreditCard> getCreditCards(String token, Account requestAccount) {
        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }
        log.info("Token and account match.");

        var creditCards = foundAccount.getCreditCards();
        if (creditCards == null) {
            log.warn("User does not have any credit cards set. Returning empty set.");
            creditCards = new HashSet<>();
        }
        log.info("Returning credit cards.");
        return creditCards;
    }

    @Transactional
    public Boolean addCreditCard(String token, Account requestAccount, CreditCard creditCard) {
        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
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
    public Boolean deleteCreditCard(String token, Account requestAccount, CreditCard creditCard) {
        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
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
     * Checks if token exists in the database and if a sailor account with id = sailorId exists
     *
     * @param token    token from cookies
     * @param sailorId the id of the account to be retrieved in the database
     * @return the account entity or null if token/sailor id are not existing in the database or the account is not corresponding to an sailor account
     */
    public Account getSailorAccountById(String token, String sailorId) {
        var foundToken = tokensRepository.findFirstByTokenValue(token);
        if (foundToken == null) {
            log.warn("Token not existing in the database.");
            return null;
        }

        long sailorIdLong;
        try {
            sailorIdLong = Long.parseLong(sailorId);
        } catch (Exception e) {
            log.error("Exception occurred while transforming sailorId String to Long", e);
            return null;
        }
        var foundAccountOptional = accountsRepository.findById(sailorIdLong);

        if (foundAccountOptional.isPresent()) {
            var foundAccount = foundAccountOptional.get();
            if (foundAccount.getType() == UserType.CLIENT) {
                log.warn("Account was found by id " + sailorId + " but the account is matching a client account, not a sailor.");
                return null;
            }
            return foundAccountOptional.get();
        }

        log.warn("No account was found by id " + sailorId);
        return null;
    }

    public String getSailorName(String token, String sailorId) {
        var foundAccount = getSailorAccountById(token, sailorId);
        if (foundAccount == null) return null;

        var accountDetails = foundAccount.getAccountDetails();
        if (accountDetails == null || accountDetails.getFullName() == null || accountDetails.getFullName().isEmpty()) {
            log.warn("Account details is null. Sailor does not have a username set yet. Returning username from Account.");
            return foundAccount.getUsername();
        }

        log.info("Found username for sailor id " + sailorId + ": " + accountDetails.getFullName());
        return accountDetails.getFullName();
    }

    @Transactional
    public Boolean pulse(String token, Account account, LocationData locationData) {
        try {
            Account foundAccount = getAccountByTokenAndCredentials(token, account);
            if (foundAccount == null) {
                log.info("Request account or token are invalid.");
                return null;
            }
            log.info("Token and credentials match.");

            if (foundAccount.getType() == UserType.CLIENT) {
                log.warn("Account and token match but account is not a sailor account.");
                return null;
            }
            log.info("Account is a sailor account.");

            var sailor = activeSailorsRepository.findFirstByAccountId(foundAccount.getId());
            if (sailor == null) {
                log.warn("Couldn't find active sailor account by id '" + foundAccount.getId() + "'");
                return null;
            }
            log.info("Sailor account found with the account id found earlier.");

            var oldLocationData = sailor.getLocationData();
            sailor.setLocationData(locationData);
            sailor.setLastUpdate(new Date());
            activeSailorsRepository.save(sailor);
            log.info("Updated active sailor location data via pulse. ");
            locationDataRepository.deleteById(oldLocationData.getId());
            log.info("Deleted old location data with id: " + oldLocationData.getId());

            log.info("Returning true");
            return true;
        } catch (Exception e) {
            log.error("Exception occurred during pulse workflow. Returning false", e);
            return false;
        }
    }
}
