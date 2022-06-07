package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.TokensRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    private final TokensRepository tokensRepository;
    private final AccountsRepository accountsRepository;


    @Autowired
    public ImagesService(TokensRepository tokensRepository, AccountsRepository accountsRepository) {
        this.tokensRepository = tokensRepository;
        this.accountsRepository = accountsRepository;
    }

    public byte[] getDefaultProfilePicture() {
        try {
            File initialFile = new File("src/main/resources/static/default_profile_pic.png");
            if (!initialFile.exists())
                throw new IOException("File doesn't exist.");
            InputStream in = new FileInputStream(initialFile);
            log.info("Returning default profile picture.");
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            log.error("Exception while retrieving default profile picture.", e);
        }
        return null;
    }

    public byte[] getSailorProfilePicture(String token, String sailorId) {
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

            var accountDetails = foundAccount.getAccountDetails();
            if (accountDetails == null) {
                log.info("Account details is null. Sailor does not have a profile picture set yet. Returning empty profile pic.");
                return new byte[0];
            }
            var image = accountDetails.getImage();
            if (image == null || image.getBytes() == null) {
                log.info("Image of account details is null. Sailor does not have a profile picture set yet. Returning empty profile pic.");
                return new byte[0];
            }

            log.info("Found profile picture for sailor id " + sailorId);
            return image.getBytes();
        }
        log.warn("No account was found by id " + sailorId);
        return null;
    }
}
