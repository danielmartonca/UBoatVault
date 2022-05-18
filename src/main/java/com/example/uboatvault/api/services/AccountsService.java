package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.AccountDetails;
import com.example.uboatvault.api.model.persistence.Image;
import com.example.uboatvault.api.repositories.AccountDetailsRepository;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.ImagesRepository;
import com.example.uboatvault.api.repositories.TokensRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccountsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final ImagesService imagesService;

    private final AccountsRepository accountsRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final TokensRepository tokensRepository;
    private final ImagesRepository imagesRepository;

    @Autowired
    public AccountsService(ImagesService imagesService, AccountsRepository accountsRepository, AccountDetailsRepository accountDetailsRepository, TokensRepository tokensRepository, ImagesRepository imagesRepository) {
        this.imagesService = imagesService;
        this.accountsRepository = accountsRepository;
        this.accountDetailsRepository = accountDetailsRepository;
        this.tokensRepository = tokensRepository;
        this.imagesRepository = imagesRepository;
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

        if (foundAccountDetails.getFullName() == null&&requestAccountDetails.getFullName()!=null) {
            foundAccountDetails.setFullName(requestAccountDetails.getFullName());
            hasChanged = true;
        } else if (foundAccountDetails.getFullName() != null && !requestAccountDetails.getFullName().isEmpty())
            if (!foundAccountDetails.getFullName().equals(requestAccountDetails.getFullName())) {
                log.info("Account details full name was '" + requestAccountDetails.getFullName() + "'. Updated it to '" + foundAccountDetails.getFullName() + "'.");
                foundAccountDetails.setFullName(requestAccountDetails.getFullName());
                hasChanged = true;
            }

        if (foundAccountDetails.getEmail() == null&&requestAccountDetails.getEmail()!=null) {
            foundAccountDetails.setEmail(requestAccountDetails.getEmail());
            hasChanged = true;
        } else if (foundAccountDetails.getEmail() != null && !requestAccountDetails.getEmail().isEmpty())
            if (!foundAccountDetails.getEmail().equals(requestAccountDetails.getEmail())) {
                log.info("Account details email was '" + requestAccountDetails.getEmail() + "'. Updated it to '" + foundAccountDetails.getEmail() + "'.");
                foundAccountDetails.setEmail(requestAccountDetails.getEmail());
                hasChanged = true;
            }

        if (foundAccountDetails.getImage() == null&&requestAccountDetails.getImage()!=null) {
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
            log.warn("Account details is null. User hasn't setup any account details. Returning only the default profile picture.");

            accountDetails = new AccountDetails();
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
}
