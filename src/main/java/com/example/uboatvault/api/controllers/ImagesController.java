package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.services.ImagesService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ImagesController {
    private final Logger log = LoggerFactory.getLogger(ImagesController.class);
    private final ImagesService imagesService;

    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }


    @GetMapping(value = "/images/getDefaultProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getDefaultProfilePicture() {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/images/getDefaultProfilePicture"));
        return imagesService.getDefaultProfilePicture();
    }

    @GetMapping(value = "/images/getSailorProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getSailorProfilePicture(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/images/getSailorProfilePicture?sailorId=" + sailorId));
        var bytes = imagesService.getSailorProfilePicture(token, sailorId);

        if (bytes == null) {
            log.info(LoggingUtils.logResponse(HttpMethod.GET, "/images/getSailorProfilePicture?sailorId=" + sailorId));
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        log.info(LoggingUtils.logResponse(HttpMethod.GET, "/images/getSailorProfilePicture?sailorId=" + sailorId, " [ bytes... ]"));
        return new ResponseEntity<>(bytes, HttpStatus.OK);
    }

    @GetMapping(value = "/images/getSailorBoatImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<List<byte[]>> getSailorBoatImages(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/images/getSailorBoatImages?sailorId=" + sailorId));
        var imagesBytesList = imagesService.getSailorBoatImages(token, sailorId);

        if (imagesBytesList == null) {
            log.info(LoggingUtils.logResponse(HttpMethod.GET, "/images/getSailorBoatImages?sailorId=" + sailorId));
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        log.info(LoggingUtils.logResponse(HttpMethod.GET, "/images/getSailorBoatImages?sailorId=" + sailorId, imagesBytesList.size() != 0 ? "[ list of bytes... ]" : "[]"));
        return new ResponseEntity<>(imagesBytesList, HttpStatus.OK);
    }
}
