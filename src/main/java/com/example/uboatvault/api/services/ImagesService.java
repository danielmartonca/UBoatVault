package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.AccountDetails;
import com.example.uboatvault.api.model.persistence.Image;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.ImagesRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    private final AccountsRepository accountsRepository;
    private final ImagesRepository imagesRepository;

    public ImagesService(AccountsRepository accountsRepository, ImagesRepository imagesRepository) {
        this.accountsRepository = accountsRepository;
        this.imagesRepository = imagesRepository;
    }

    public byte[] getDefaultProfilePicture() {
        try {
            File initialFile = new File("src/main/resources/default_picture.png");
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

    public byte[] getProfilePicture(Account account) {
        if (account.getAccountDetails() == null) {
            log.warn("Account doesn't have an profile picture set yet.");
            return getDefaultProfilePicture();
        }

        return account.getAccountDetails().getImage().getBytes();
    }

    @Transactional
    public boolean uploadProfilePicture(Account foundAccount, byte[] imagesBytes) {
        try {
            if (foundAccount.getAccountDetails() == null)
                foundAccount.setAccountDetails(new AccountDetails());

            Image image = new Image(imagesBytes);
            imagesRepository.delete(foundAccount.getAccountDetails().getImage());
            foundAccount.getAccountDetails().setImage(image);
            accountsRepository.save(foundAccount);
            return true;
        } catch (Exception e) {
            log.error("Exception while uploading default profile picture.", e);
        }
        return false;
    }
}
