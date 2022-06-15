package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.services.ImagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        return imagesService.getDefaultProfilePicture();
    }

    @GetMapping(value = "/images/getSailorProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getSailorProfilePicture(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        var bytes = imagesService.getSailorProfilePicture(sailorId);

        if (bytes == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);


        return new ResponseEntity<>(bytes, HttpStatus.OK);
    }

    @GetMapping(value = "/images/getSailorBoatImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<List<byte[]>> getSailorBoatImages(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        var imagesBytesList = imagesService.getSailorBoatImages(sailorId);

        if (imagesBytesList == null)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(imagesBytesList, HttpStatus.OK);
    }
}
