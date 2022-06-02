package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.Token;
import com.example.uboatvault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {
    private final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final EncryptionService encryptionService;
    private final TokensRepository tokensRepository;
    private final PendingTokenRepository pendingTokenRepository;
    private final AccountsRepository accountsRepository;

    @Autowired
    public TokenService(EncryptionService encryptionService, TokensRepository tokensRepository, PendingTokenRepository pendingTokenRepository, AccountsRepository accountsRepository) {
        this.encryptionService = encryptionService;
        this.tokensRepository = tokensRepository;
        this.pendingTokenRepository = pendingTokenRepository;
        this.accountsRepository = accountsRepository;
    }


    public boolean isTokenInvalid(String token) {
        String decryptedToken = encryptionService.decryptString(token);
        return decryptedToken.isEmpty();
    }

    private long getMinuteDifferenceFromNow(Date date) {
        Date dateNow = new Date(System.currentTimeMillis());
        Date dateCreation = new Date(date.getTime());

        DateFormat format = new SimpleDateFormat("mm");
        format.format(dateNow);
        format.format(dateCreation);

        return dateNow.getTime() - dateCreation.getTime();
    }

    public boolean isTokenDeprecated(Account account) {
//        return getMinuteDifferenceFromNow(account.getToken().getTokenCreation()) > 30;//TODO
        return false;
    }

    @Transactional
    public void updateToken(Account account) {
        //detach old entity
        Token oldToken = account.getToken();
        oldToken.setAccount(null);
        account.setToken(null);

        //generate new token
        String token = generateTokenString();
        account.setToken(new Token(token));

        tokensRepository.delete(oldToken);
        accountsRepository.save(account);
        log.info("Generated new token for account.");
    }

    @Transactional
    public String generateTokenString() {
        String token;
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (tokensRepository.findFirstByTokenValue(token) != null) token = "";
            if (pendingTokenRepository.findFirstByTokenValue(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }

    @Transactional
    public String requestToken(Account account) {
        var foundAccount = accountsRepository.findFirstByPassword(account.getPassword());
        if (foundAccount != null) {
            if (!foundAccount.getPhoneNumber().equals(account.getPhoneNumber()) &&
                    !foundAccount.getUsername().equals(account.getUsername())) {
                log.warn("Account found by password but username or phone number don't match.");
                return null;
            } else if (!foundAccount.getRegistrationData().equals(account.getRegistrationData())) {
                log.warn("Account found, username and phone number matched but registration data did not.");
            } else {
                log.info("Account found, username phone number and registration data matched.");
            }

            if (isTokenDeprecated(foundAccount))
                updateToken(foundAccount);

            return foundAccount.getToken().getTokenValue();
        }
        log.info("No account was found with the given username/phone number and password.");
        return null;
    }
}
