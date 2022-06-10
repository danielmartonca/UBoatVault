package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.repositories.AccountsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {
    private final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final TokenService tokenService;

    private final AccountsRepository accountsRepository;

    @Autowired
    public LoginService(TokenService tokenService, AccountsRepository accountsRepository) {
        this.tokenService = tokenService;
        this.accountsRepository = accountsRepository;
    }

    @Transactional
    public String login(Account account, String token) {
        var foundAccountsList = accountsRepository.findAllByPassword(account.getPassword());
        if (foundAccountsList == null) {
            log.warn("No account was found with the given username/phone number and password.");
            return null;
        }

        for (var foundAccount : foundAccountsList) {
            if (!foundAccount.getToken().getTokenValue().equals(token)) {
                log.warn("An account with given password found but tokens do not match.");
            } else if (foundAccount.getUsername().equals(account.getUsername()) || foundAccount.getPhoneNumber().equals(account.getPhoneNumber())) {
                log.info("Credentials matched. Found account.");
                tokenService.updateToken(foundAccount);
                log.info("Login successful. Returning token: " + foundAccount.getToken().getTokenValue());
                return foundAccount.getToken().getTokenValue();
            } else
                log.warn("An account with given password found but neither username or phone number match.");

        }
        log.warn("Invalid credentials. Login failed");
        return null;
    }
}
