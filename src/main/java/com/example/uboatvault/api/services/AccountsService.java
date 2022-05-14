package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.repositories.TokensRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final TokensRepository tokensRepository;

    @Autowired
    public AccountsService(TokensRepository tokensRepository) {
        this.tokensRepository = tokensRepository;
    }


    public Account getAccountByTokenAndCredentials(String token, Account requestAccount) {
        Account foundAccount;

        var foundToken = tokensRepository.findFirstByTokenValue(token);
        if (foundToken == null) {
            log.warn("Token not existing in the database.");
            return null;
        }

        foundAccount = foundToken.getAccount();

        if (!foundAccount.getPassword().equals(requestAccount.getPassword())) {
            log.warn("Passwords don't match.");
            return null;
        }

        if (!foundAccount.getUsername().equals(requestAccount.getUsername()) && !foundAccount.getPhoneNumber().equals(requestAccount.getPhoneNumber())) {
            log.warn("Password matches but neither username or phone number match.");
            return null;
        }

        log.info("Credentials are ok. Account retrieved from database is sent back to the user.");
        return foundAccount;
    }
}
