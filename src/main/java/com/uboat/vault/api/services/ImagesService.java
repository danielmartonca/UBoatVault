package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.persistence.sailing.sailor.BoatImage;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.repositories.BoatImagesRepository;
import com.uboat.vault.api.repositories.BoatsRepository;
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
import java.util.NoSuchElementException;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    private final EntityService entityService;
    private final JwtService jwtService;

    private final AccountsRepository accountsRepository;
    private final SailorsRepository sailorsRepository;
    private final BoatsRepository boatsRepository;
    private final BoatImagesRepository boatImagesRepository;

    @Autowired
    public ImagesService(EntityService entityService, JwtService jwtService, AccountsRepository accountsRepository, SailorsRepository sailorsRepository, BoatsRepository boatsRepository, BoatImagesRepository boatImagesRepository) {
        this.entityService = entityService;
        this.jwtService = jwtService;
        this.accountsRepository = accountsRepository;
        this.sailorsRepository = sailorsRepository;
        this.boatsRepository = boatsRepository;
        this.boatImagesRepository = boatImagesRepository;
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

    public UBoatResponse getProfilePicture(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //can't be null because it is initialized when creating account
            var image = account.getAccountDetails().getImage();

            return new UBoatResponse(UBoatStatus.PROFILE_PICTURE_RETRIEVED, image.getBytes());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving profile picture", e);
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

    public UBoatResponse getSailorProfilePicture(String sailorId) {
        try {
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null) return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);

            var accountOptional = accountsRepository.findById(sailor.getAccountId());
            if (accountOptional.isEmpty())
                throw new RuntimeException("Warning: sailor has account id which does not belong to any account");

            //cant be null
            var accountDetails = accountOptional.get().getAccountDetails();

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

    @Transactional
    public UBoatResponse uploadBoatImage(String authorizationHeader, byte[] imageBytes, String contentType) {
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

            var newBoatImage = new BoatImage(contentType, imageBytes, boat);
            boatImages.add(newBoatImage);
            sailorsRepository.save(sailor);

            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_UPLOADED, newBoatImage.getHash());
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while uploading a boat image", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional //has to be transactional due to blob images
    public UBoatResponse getBoatImagesIdentifiers(String sailorId) {
        try {
            //cant be null because the operation is already done in the filter before
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null)
                return new UBoatResponse(UBoatStatus.SAILOR_NOT_FOUND);

            var boatImages = sailor.getBoat().getBoatImages();

            var list = boatImages.stream().map(BoatImage::getHash).toList();
            if (list.isEmpty()) return new UBoatResponse(UBoatStatus.BOAT_IMAGES_HASHES_EMPTY, list);

            return new UBoatResponse(UBoatStatus.BOAT_IMAGES_HASHES_RETRIEVED, list);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat images hashes.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional //has to be transactional due to blob images
    public UBoatResponse getBoatImage(String authorizationHeader, String identifier) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());

            //sailors are not allowed to access other sailors images
            if (account.getType() == UserType.SAILOR) {
                var sailor = entityService.findSailorByCredentials(account);
                var image = sailor.getBoat().getBoatImages().stream()
                        .filter(boatImage -> boatImage.getHash().equals(identifier))
                        .findFirst()
                        .orElseThrow();
                return new UBoatResponse(UBoatStatus.BOAT_IMAGE_RETRIEVED, image.getBytes());
            }

            var image = boatImagesRepository.findBoatImageByHash(identifier);
            if (image == null) throw new NoSuchElementException("Boat image not found by identifier.");

            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_RETRIEVED, image.getBytes());
        } catch (NoSuchElementException e) {
            log.warn("Boat image not found by identifier.");
            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_NOT_FOUND);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat image.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatResponse deleteBoatImage(String authorizationHeader, String identifier) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByUsername(jwtData.username());
            var sailor = entityService.findSailorByCredentials(account);
            var boat = sailor.getBoat();
            var sailorImages = boat.getBoatImages();

            var image = sailorImages.stream()
                    .filter(boatImage -> boatImage.getHash().equals(identifier))
                    .findFirst()
                    .orElseThrow();

            sailorImages.remove(image);
            boatsRepository.saveAndFlush(boat);
            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_DELETED, true);
        } catch (NoSuchElementException e) {
            log.warn("Boat image not found by identifier to the account in the JWT.");
            return new UBoatResponse(UBoatStatus.BOAT_IMAGE_NOT_FOUND, false);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatResponse(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while deleting the boat image by hash.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }
}
