package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.TokensRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    private final AccountsService accountsService;


    @Autowired
    public ImagesService(@Lazy AccountsService accountsService) {
        this.accountsService = accountsService;
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
        var foundAccount = accountsService.getSailorAccountById(token, sailorId);
        if (foundAccount == null)
            return null;

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
}
