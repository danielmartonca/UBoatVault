package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.repositories.AccountsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String login(Account account, String token) {
        var foundAccount = accountsRepository.findFirstByPassword(account.getPassword());
        if (foundAccount != null &&
                foundAccount.getRegistrationData().equals(account.getRegistrationData()) &&
                foundAccount.getToken().getTokenValue().equals(token))
            if (foundAccount.getUsername().equals(account.getUsername()) ||
                    foundAccount.getPhoneNumber().equals(account.getPhoneNumber())) {
                log.info("Found account. Updating token and returning it.");
                tokenService.updateToken(account);
                return account.getToken().getTokenValue();
            }
        log.warn("Invalid credentials. Login failed");
        return null;
    }
}
