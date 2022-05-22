package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.AccountDetails;
import com.example.uboatvault.api.model.persistence.CreditCard;
import com.example.uboatvault.api.model.persistence.Image;
import com.example.uboatvault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
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

    @Autowired
    public AccountsService(ImagesService imagesService, AccountsRepository accountsRepository, AccountDetailsRepository accountDetailsRepository, TokensRepository tokensRepository, ImagesRepository imagesRepository, CreditCardsRepository creditCardsRepository) {
        this.imagesService = imagesService;
        this.accountsRepository = accountsRepository;
        this.accountDetailsRepository = accountDetailsRepository;
        this.tokensRepository = tokensRepository;
        this.imagesRepository = imagesRepository;
        this.creditCardsRepository = creditCardsRepository;
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
            foundAccountDetails.setImage(requestAccountDetails.getImage());
            foundAccountDetails.getImage().setAccountDetails(foundAccountDetails);
            hasChanged = true;
        } else if (foundAccountDetails.getImage() != null && requestAccountDetails.getImage().getBytes() != null)
            if (requestAccountDetails.getImage().getBytes().length != 0) {
                log.info("Updating profile picture.");
                var image = foundAccountDetails.getImage();
                imagesRepository.deleteById(image.getId());
                var newImage = requestAccountDetails.getImage();
                foundAccountDetails.setImage(newImage);
                hasChanged = true;
            }

        if (hasChanged) {
            accountDetailsRepository.save(foundAccountDetails);
            log.info("Updated database account details.");
        } else
            log.info("Account details were identical.");
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
        }
        if (accountDetails.getImage() == null) {
            log.warn("Account details image is null. Setting up default profile picture.");
            var imageBytes = imagesService.getDefaultProfilePicture();
            var image = new Image(imageBytes);

            accountDetails.setImage(image);
            image.setAccountDetails(accountDetails);
            foundAccount.setAccountDetails(accountDetails);
            accountsRepository.save(foundAccount);
            return accountDetails;
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
            return null;
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
    public boolean addCreditCard(String token, Account requestAccount, CreditCard creditCard) {
        Account foundAccount = getAccountByTokenAndCredentials(token, requestAccount);
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return false;
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
        }
        userCreditCards.add(creditCard);

        accountsRepository.save(foundAccount);
        log.info("Saved new credit card in the database.");
        return true;
    }
}
