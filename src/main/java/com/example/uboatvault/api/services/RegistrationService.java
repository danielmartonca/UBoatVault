package com.example.uboatvault.api.services;

import com.example.uboatvault.api.controllers.RegistrationController;
import com.example.uboatvault.api.model.PendingToken;
import com.example.uboatvault.api.model.RegistrationData;
import com.example.uboatvault.api.model.SimCard;
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

    @Autowired
    public RegistrationService(RegistrationDataRepository registrationDataRepository, SimCardRepository simCardRepository, PendingTokenRepository pendingTokenRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.simCardRepository = simCardRepository;
        this.pendingTokenRepository = pendingTokenRepository;
    }

    private long getMinuteDifferenceFromNow(Date date) {
        Date now = new Date(System.currentTimeMillis());
        return now.getTime() - date.getTime();
    }

    private String generateToken() {
        String token = "";
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (registrationDataRepository.findFirstByToken(token) != null) token = "";
            if (pendingTokenRepository.findFirstByTokenValue(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }


    /**
     * This method searches if any part of the registrationData exists in the database and returns its token if found (or generates a new one if it's older than 30 minutes)
     */
    public String searchForTokenBasedOnRegistrationData(RegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            if (getMinuteDifferenceFromNow(foundRegistrationData.getTokenCreation()) > 30) {
                foundRegistrationData.setToken(generateToken());
                foundRegistrationData.setTokenCreation(new Date(System.currentTimeMillis()));
                registrationDataRepository.save(foundRegistrationData);
            }
            return foundRegistrationData.getToken();
        }

        for (var simCard : registrationData.getMobileNumbersInfoList()) {
            SimCard extractedSimCard = simCardRepository.findFirstByNumberAndDisplayNameAndCountryIso(simCard.getNumber(), simCard.getDisplayName(), simCard.getCountryIso());
            if (extractedSimCard != null) {
                if (getMinuteDifferenceFromNow(registrationData.getTokenCreation()) > 30) {
                    registrationData.setToken(generateToken());
                    registrationData.setTokenCreation(new Date(System.currentTimeMillis()));
                    registrationDataRepository.save(registrationData);
                }
                return registrationData.getToken();
            }
        }

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
                return foundRegistrationData.getToken();
            }

        return null;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    public String requestRegistration(RegistrationData registrationData) {
        try {
            String token = searchForTokenBasedOnRegistrationData(registrationData);
            if (token != null)
                return token;
            return generateToken();
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return null;
        }
    }

    /**
     * Saves a new device in the database based on registrationData and returns its token if successful.
     */
    public String register(RegistrationData registrationData, String token) {
        try {
            if (pendingTokenRepository.findFirstByTokenValue(token) == null) {
                registrationData.setToken(token);
                registrationData.setTokenCreation(new Date(System.currentTimeMillis()));
                registrationData = registrationDataRepository.save(registrationData);
                pendingTokenRepository.deleteByTokenValue(token);
                return registrationData.getToken();
            }
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
        }
        return null;
    }
}
