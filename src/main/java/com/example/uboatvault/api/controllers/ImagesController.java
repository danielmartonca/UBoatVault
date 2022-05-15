package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.requests.ImageUploadRequest;
import com.example.uboatvault.api.services.AccountsService;
import com.example.uboatvault.api.services.ImagesService;
import com.example.uboatvault.api.services.TokenService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ImagesController {
    private final Logger log = LoggerFactory.getLogger(ImagesController.class);
    private final ImagesService imagesService;
    private final TokenService tokenService;
    private final AccountsService accountsService;

    public ImagesController(ImagesService imagesService, TokenService tokenService, AccountsService accountsService) {
        this.imagesService = imagesService;
        this.tokenService = tokenService;
        this.accountsService = accountsService;
    }


    @GetMapping(value = "/images/getDefaultProfilePicture",produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getDefaultProfilePicture() {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/api/getDefaultProfilePicture", null));
        return imagesService.getDefaultProfilePicture();
    }

    @PostMapping(value = "/images/getProfilePicture",produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getProfilePicture(@CookieValue(name = "token") String token,
                             @RequestBody Account requestAccount) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getProfilePicture", requestAccount));
        if (tokenService.isTokenInvalid(token)) {
            log.warn("Token is not valid.");
            return null;
        }
        var foundAccount = accountsService.getAccountByTokenAndCredentials(token, requestAccount);
        if (foundAccount == null) {
            log.warn("Invalid token or credentials. Returning null.");
            return null;
        }

        log.info("Credentials and token match. Returning profile picture.");
        return imagesService.getProfilePicture(foundAccount);
    }

    @PutMapping(value = "/images/uploadProfilePicture")
    public ResponseEntity<Boolean> uploadProfilePicture(@CookieValue(name = "token") String token,
                                                        @RequestBody ImageUploadRequest imageUploadRequest) {
        log.info(LoggingUtils.logRequest(HttpMethod.PUT, "/api/uploadProfilePicture", imageUploadRequest));

        if (tokenService.isTokenInvalid(token)) {
            log.warn("Token is not valid.");
            return null;
        }
        var foundAccount = accountsService.getAccountByTokenAndCredentials(token, imageUploadRequest.getAccount());
        if (foundAccount == null) {
            log.warn("Invalid token or credentials. Returning null.");
            return null;
        }
        log.info("Credentials and token match. Uploading new profile picture.");

        var hasUploaded = imagesService.uploadProfilePicture(foundAccount,imageUploadRequest.getImageBytes());
        if (hasUploaded) {
            log.info("New profile picture updated successfully.");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            log.info("Failed to update profile picture.");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }
}
