package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.Token;
import com.example.uboatvault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {
    private final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final RegistrationDataRepository registrationDataRepository;
    private final EncryptionService encryptionService;
    private final TokensRepository tokensRepository;
    private final PendingTokenRepository pendingTokenRepository;
    private final PendingAccountsRepository pendingAccountsRepository;
    private final AccountsRepository accountsRepository;
    private final PhoneNumbersRepository phoneNumbersRepository;

    @Autowired
    public TokenService(RegistrationDataRepository registrationDataRepository, EncryptionService encryptionService, TokensRepository tokensRepository, PendingTokenRepository pendingTokenRepository, PendingAccountsRepository pendingAccountsRepository, AccountsRepository accountsRepository, PhoneNumbersRepository phoneNumbersRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.encryptionService = encryptionService;
        this.tokensRepository = tokensRepository;
        this.pendingTokenRepository = pendingTokenRepository;
        this.pendingAccountsRepository = pendingAccountsRepository;
        this.accountsRepository = accountsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
    }


    public boolean isTokenDecryptable(String token) {
        String decryptedToken = encryptionService.decryptString(token);
        return !decryptedToken.isEmpty();
    }

    private long getMinuteDifferenceFromNow(Date date) {
        Date now = new Date(System.currentTimeMillis());
        return now.getTime() - date.getTime();
    }

    public boolean isTokenDeprecated(Account account) {
        return getMinuteDifferenceFromNow(account.getToken().getTokenCreation()) > 30;
    }

    public void updateToken(Account account) {
        String token = generateToken();
        account.setToken(new Token(token));
    }

    public String generateToken() {
        String token;
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (tokensRepository.findFirstByTokenValue(token) != null) token = "";
            if (pendingTokenRepository.findFirstByTokenValue(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }

    public String requestToken(Account account) {
        var foundAccount = accountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
        if (foundAccount != null &&
                foundAccount.getPhoneNumber().equals(account.getPhoneNumber()) &&
                foundAccount.getRegistrationData().equals(account.getRegistrationData())) {
            updateToken(account);
            return account.getToken().getTokenValue();
        }
        return null;
    }
}
