package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.persistence.sailing.sailor.BoatImage;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.repositories.SailorsRepository;
import com.uboat.vault.api.utilities.HashUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    private final EntityService entityService;
    private final JwtService jwtService;

    private final AccountsRepository accountsRepository;
    private final SailorsRepository sailorsRepository;

    @Autowired
    public ImagesService(EntityService entityService, JwtService jwtService, AccountsRepository accountsRepository, SailorsRepository sailorsRepository) {
        this.entityService = entityService;
        this.jwtService = jwtService;
        this.accountsRepository = accountsRepository;
        this.sailorsRepository = sailorsRepository;
    }

    public UBoatResponse getDefaultProfilePicture() {
        try {
            var defaultProfilePictureFile = new File("src/main/resources/static/default_profile_pic.png");
            if (!defaultProfilePictureFile.exists())
                throw new IOException("Default profile picture file doesn't exist.");

            InputStream in = new FileInputStream(defaultProfilePictureFile);

            log.info("Returning default profile picture.");
            return new UBoatResponse(UBoatStatus.DEFAULT_PROFILE_PICTURE_RETRIEVED, IOUtils.toByteArray(in));
        } catch (IOException e) {
            log.error("Exception while retrieving default profile picture.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatResponse getSailorProfilePicture(String sailorId) {
        try {
            var account = entityService.findSailorAccountById(sailorId);
            if (account == null) return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);

            var accountDetails = account.getAccountDetails();
            if (accountDetails == null) {
                log.info("Account details is null. Sailor does not have a profile picture set yet. Returning empty profile pic.");
                return new UBoatResponse(UBoatStatus.SAILOR_PROFILE_PICTURE_NOT_SET, new byte[0]);
            }

            var image = accountDetails.getImage();
            if (image == null || image.getBytes() == null) {
                log.info("Image of account details is null. Sailor does not have a profile picture set yet. Returning empty profile pic.");
                return new UBoatResponse(UBoatStatus.SAILOR_PROFILE_PICTURE_NOT_SET, new byte[0]);
            }

            log.info("Found profile picture for sailor id " + sailorId);
            return new UBoatResponse(UBoatStatus.SAILOR_DETAILS_RETRIEVED, image.getBytes());
        } catch (Exception e) {
            log.error("Exception occurred while retrieving sailor profile picture.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatResponse getSailorBoatImages(String sailorId) {
        try {
            var sailor = entityService.findSailorBySailorId(sailorId);

            if (sailor == null) return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);

            var imagesBytes = sailor.getBoat().getBoatImages().stream().map(BoatImage::getBytes).toList();
            log.info("Found {} boat images for sailor id {}", imagesBytes.size(), sailorId);

            return new UBoatResponse(UBoatStatus.SAILOR_BOAT_IMAGES_RETRIEVED, imagesBytes);
        } catch (Exception e) {
            log.error("Exception occurred while retrieving sailor boat images.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatResponse uploadProfileImage(String authorizationHeader, byte[] imageBytes) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //can't be null because it is initialized when creating account
            var image = account.getAccountDetails().getImage();
            var hash = HashUtils.calculateHash(imageBytes);
            if (image.getHash().equals(hash)) {
                log.info("Profile image is already bound to the account.");
                return new UBoatResponse(UBoatStatus.PROFILE_IMAGE_ALREADY_EXISTING, true);
            }

            log.info("New profile picture has been uploaded.");
            image.setBytes(imageBytes);
            accountsRepository.save(account);
            return new UBoatResponse(UBoatStatus.PROFILE_IMAGE_UPLOADED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while uploading a client profile picture", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional
    public UBoatResponse uploadBoatImage(String authorizationHeader, byte[] imageBytes) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());
            var sailor = entityService.findSailorByCredentials(account);
            var boat = sailor.getBoat();

            var boatImages = boat.getBoatImages();
            var hash = HashUtils.calculateHash(imageBytes);

            if (boatImages.stream().map(BoatImage::getHash).anyMatch(hash::equals))
                return new UBoatResponse(UBoatStatus.BOAT_IMAGE_ALREADY_EXISTING, true);

            var newBoatImage = new BoatImage(imageBytes, boat);
            boatImages.add(newBoatImage);
            sailorsRepository.save(sailor);

            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_UPLOADED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while uploading a boat image", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    public UBoatResponse getBoatImagesIdentifiers(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());
            var sailor = entityService.findSailorByCredentials(account);
            var boatImages = sailor.getBoat().getBoatImages();

            var list = boatImages.stream().map(BoatImage::getHash).toList();
            if (list.isEmpty()) return new UBoatResponse(UBoatStatus.BOAT_IMAGES_HASHES_EMPTY, list);

            return new UBoatResponse(UBoatStatus.BOAT_IMAGES_HASHES_RETRIEVED, list);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat images hashes.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }
}
