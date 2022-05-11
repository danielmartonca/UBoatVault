package com.example.uboatvault.api.services;

import com.example.uboatvault.api.controllers.RegistrationController;
import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.PendingToken;
import com.example.uboatvault.api.model.persistence.RegistrationData;
import com.example.uboatvault.api.model.persistence.SimCard;
import com.example.uboatvault.api.model.requests.RegistrationRequest;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.PendingTokenRepository;
import com.example.uboatvault.api.repositories.RegistrationDataRepository;
import com.example.uboatvault.api.repositories.SimCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class RegistrationService {
    private final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationDataRepository registrationDataRepository;
    private final SimCardRepository simCardRepository;
    private final PendingTokenRepository pendingTokenRepository;
    private final AccountsRepository accountsRepository;

    @Autowired
    public RegistrationService(RegistrationDataRepository registrationDataRepository, SimCardRepository simCardRepository, PendingTokenRepository pendingTokenRepository, AccountsRepository accountsRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.simCardRepository = simCardRepository;
        this.pendingTokenRepository = pendingTokenRepository;
        this.accountsRepository = accountsRepository;
    }

    private long getMinuteDifferenceFromNow(Date date) {
        Date now = new Date(System.currentTimeMillis());
        return now.getTime() - date.getTime();
    }

    private String generateToken() {
        String token;
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (registrationDataRepository.findFirstByToken(token) != null) token = "";
            if (pendingTokenRepository.findFirstByTokenValue(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }

    private boolean isTokenDeprecated(RegistrationData registrationData) {
        return getMinuteDifferenceFromNow(registrationData.getTokenCreation()) > 30;
    }

    private void updateToken(RegistrationData registrationData) {
        registrationData.setToken(generateToken());
        registrationData.setTokenCreation(new Date(System.currentTimeMillis()));
    }

    /**
     * This method searches if any part of the registrationData exists in the database and returns its token if found (or generates a new one if it's older than 30 minutes)
     */
    public String searchForTokenBasedOnRegistrationData(RegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            if (isTokenDeprecated(foundRegistrationData)) {
                updateToken(foundRegistrationData);
                registrationDataRepository.save(foundRegistrationData);
            }
            log.info("Found token.");
            return foundRegistrationData.getToken();
        }

        for (var simCard : registrationData.getMobileNumbersInfoList()) {
            SimCard extractedSimCard = simCardRepository.findFirstByNumberAndDisplayNameAndCountryIso(simCard.getNumber(), simCard.getDisplayName(), simCard.getCountryIso());
            if (extractedSimCard != null) {
                if (isTokenDeprecated(registrationData)) {
                    updateToken(registrationData);
                    registrationDataRepository.save(registrationData);
                }
                log.info("Found token.");
                return registrationData.getToken();
            }
        }
        log.info("Couldn't find any token.");
        return null;
    }

    /**
     * This method check if the token passed as parameter corresponds to registrationData in the database
     * If they match and token is older than 30 minutes, a new token will be generated, and it will update the database.
     */
    public String searchForToken(RegistrationData registrationData, String token) {
        RegistrationData foundRegistrationData = registrationDataRepository.findFirstByToken(token);
        if (!foundRegistrationData.equals(registrationData))
            if (getMinuteDifferenceFromNow(foundRegistrationData.getTokenCreation()) > 30) {
                foundRegistrationData.setToken(generateToken());
                foundRegistrationData.setTokenCreation(new Date(System.currentTimeMillis()));
                registrationDataRepository.save(foundRegistrationData);
                log.info("Found token.");
                return foundRegistrationData.getToken();
            }
        log.info("Couldn't find any token.");
        return null;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    public String requestRegistration(RegistrationData registrationData) {
        try {
            String token = searchForTokenBasedOnRegistrationData(registrationData);
            if (token == null)
                token = generateToken();
            pendingTokenRepository.save(new PendingToken(token));
            log.info("Created registration request. Returning token '" + token + "'.");
            return token;
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return null;
        }
    }

    /**
     * Checks if an account exists with the given username in the database.
     */
    public boolean isUsernameUsed(String username) {
        boolean usernameExists = true;
        Account account = accountsRepository.findFirstByUsername(username);
        if (account == null)
            usernameExists = false;
        log.info(usernameExists ? "Username is already used." : "Username is not used.");
        return usernameExists;
    }

    /**
     * Saves a new device in the database based on registrationData and returns its token if successful.
     */
    public String register(RegistrationRequest registrationRequest, String token) {
        try {
            RegistrationData registrationData = registrationRequest.getRegistrationData();
            if (pendingTokenRepository.findFirstByTokenValue(token) == null) {
                updateToken(registrationData);
                registrationDataRepository.save(registrationData);
                pendingTokenRepository.deleteByTokenValue(token);
                log.info("Registration successful. Returning token '" + token + "'.");
                return registrationData.getToken();
            }
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
        }
        return null;
    }
}
