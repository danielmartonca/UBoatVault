package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.sailor.BoatImage;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.BoatImagesRepository;
import com.uboat.vault.api.persistence.repostiories.BoatsRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import com.uboat.vault.api.utilities.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImagesService {

    private final EntityService entityService;
    private final JwtService jwtService;

    private final AccountsRepository accountsRepository;
    private final SailorsRepository sailorsRepository;
    private final BoatsRepository boatsRepository;
    private final BoatImagesRepository boatImagesRepository;

    public UBoatDTO getDefaultProfilePicture() {
        try {
            var defaultProfilePictureFile = new File("src/main/resources/static/default_profile_pic.png");
            if (!defaultProfilePictureFile.exists())
                throw new IOException("Default profile picture file doesn't exist.");

            InputStream in = new FileInputStream(defaultProfilePictureFile);

            log.info("Returning default profile picture.");
            return new UBoatDTO(UBoatStatus.DEFAULT_PROFILE_PICTURE_RETRIEVED, IOUtils.toByteArray(in));
        } catch (IOException e) {
            log.error("Exception while retrieving default profile picture.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatDTO getProfilePicture(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //can't be null because it is initialized when creating account
            var image = account.getAccountDetails().getImage();

            return new UBoatDTO(UBoatStatus.PROFILE_PICTURE_RETRIEVED, image.getBytes());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving profile picture", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO uploadProfileImage(String authorizationHeader, byte[] imageBytes) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //can't be null because it is initialized when creating account
            var image = account.getAccountDetails().getImage();
            var hash = HashUtils.calculateHash(imageBytes);
            if (image.getHash() != null && image.getHash().equals(hash)) {
                log.info("Profile image is already bound to the account.");
                return new UBoatDTO(UBoatStatus.PROFILE_IMAGE_ALREADY_EXISTING, true);
            }

            log.info("New profile picture has been uploaded.");
            image.setBytes(imageBytes);
            accountsRepository.save(account);
            return new UBoatDTO(UBoatStatus.PROFILE_IMAGE_UPLOADED, true);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus(), false);
        } catch (Exception e) {
            log.error("An exception occurred while uploading a client profile picture", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    public UBoatDTO getSailorProfilePicture(String sailorId) {
        try {
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null) return new UBoatDTO(UBoatStatus.SAILOR_NOT_FOUND);

            var accountDetails = sailor.getAccount().getAccountDetails();

            var image = accountDetails.getImage();
            if (image == null || image.getBytes() == null) {
                log.info("Image of account details is null. Sailor does not have a profile picture set yet. Returning empty profile pic.");
                return new UBoatDTO(UBoatStatus.SAILOR_PROFILE_PICTURE_NOT_SET, new byte[0]);
            }

            log.info("Found profile picture for sailor id " + sailorId);
            return new UBoatDTO(UBoatStatus.SAILOR_DETAILS_RETRIEVED, image.getBytes());
        } catch (Exception e) {
            log.error("Exception occurred while retrieving sailor profile picture.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO uploadBoatImage(String authorizationHeader, byte[] imageBytes, String contentType) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);
            var sailor = entityService.findSailorByCredentials(account);
            var boat = sailor.getBoat();

            var boatImages = boat.getBoatImages();
            var hash = HashUtils.calculateHash(imageBytes);

            if (boatImages.stream().map(BoatImage::getHash).anyMatch(hash::equals))
                return new UBoatDTO(UBoatStatus.BOAT_IMAGE_ALREADY_EXISTING, boatImages.stream().filter(image -> image.getHash().equals(hash)).findFirst().orElseThrow().getHash());

            var newBoatImage = new BoatImage(contentType, imageBytes, boat);
            boatImages.add(newBoatImage);
            sailorsRepository.save(sailor);

            return new UBoatDTO(UBoatStatus.BOAT_IMAGE_UPLOADED, newBoatImage.getHash());
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while uploading a boat image", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    private UBoatDTO clientGetBoatImagesIdentifiers(String sailorId) {
        try {
            //cant be null because the operation is already done in the filter before
            var sailor = entityService.findSailorBySailorId(sailorId);
            if (sailor == null)
                return new UBoatDTO(UBoatStatus.SAILOR_NOT_FOUND);

            var boatImages = sailor.getBoat().getBoatImages();

            var list = boatImages.stream().map(BoatImage::getHash).toList();
            if (list.isEmpty()) return new UBoatDTO(UBoatStatus.BOAT_IMAGES_HASHES_EMPTY, list);

            return new UBoatDTO(UBoatStatus.BOAT_IMAGES_HASHES_RETRIEVED, list);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat images hashes for client.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    private UBoatDTO sailorGetBoatImagesIdentifiers(String authorizationHeader) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);
            var sailor = entityService.findSailorByCredentials(account);

            var boatImages = sailor.getBoat().getBoatImages();

            var list = boatImages.stream().map(BoatImage::getHash).toList();
            if (list.isEmpty()) return new UBoatDTO(UBoatStatus.BOAT_IMAGES_HASHES_EMPTY, list);

            return new UBoatDTO(UBoatStatus.BOAT_IMAGES_HASHES_RETRIEVED, list);
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat images hashes for sailor.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }

    @Transactional //has to be transactional due to blob images
    public UBoatDTO getBoatImagesIdentifiers(String authorizationHeader, String sailorId) {
        if (sailorId == null) return sailorGetBoatImagesIdentifiers(authorizationHeader);
        return clientGetBoatImagesIdentifiers(sailorId);
    }

    @Transactional //has to be transactional due to blob images
    public UBoatDTO getBoatImage(String authorizationHeader, String identifier) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            //sailors are not allowed to access other sailors images
            if (account.getType() == UserType.SAILOR) {
                var sailor = entityService.findSailorByCredentials(account);
                var image = sailor.getBoat().getBoatImages().stream()
                        .filter(boatImage -> boatImage.getHash().equals(identifier))
                        .findFirst()
                        .orElseThrow();
                return new UBoatDTO(UBoatStatus.BOAT_IMAGE_RETRIEVED, image.getBytes());
            }

            var image = boatImagesRepository.findBoatImageByHash(identifier);
            if (image == null) throw new NoSuchElementException("Boat image not found by identifier.");

            return new UBoatDTO(UBoatStatus.BOAT_IMAGE_RETRIEVED, image.getBytes());
        } catch (NoSuchElementException e) {
            log.warn("Boat image not found by identifier.");
            return new UBoatDTO(UBoatStatus.BOAT_IMAGE_NOT_FOUND);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while retrieving boat image.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO deleteBoatImage(String authorizationHeader, String identifier) {
        try {
            //cant be null because the operation is already done in the filter before
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);
            var sailor = entityService.findSailorByCredentials(account);
            var boat = sailor.getBoat();
            var sailorImages = boat.getBoatImages();

            var image = sailorImages.stream()
                    .filter(boatImage -> boatImage.getHash().equals(identifier))
                    .findFirst()
                    .orElseThrow();

            sailorImages.remove(image);
            boatsRepository.saveAndFlush(boat);
            return new UBoatDTO(UBoatStatus.BOAT_IMAGE_DELETED, true);
        } catch (NoSuchElementException e) {
            log.warn("Boat image not found by identifier to the account in the JWT.");
            return new UBoatDTO(UBoatStatus.BOAT_IMAGE_NOT_FOUND, false);
        } catch (UBoatJwtException e) {
            log.error("Exception occurred during Authorization Header/JWT processing.", e);
            return new UBoatDTO(e.getStatus());
        } catch (Exception e) {
            log.error("An exception occurred while deleting the boat image by hash.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR, false);
        }
    }
}
