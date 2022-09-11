package com.uboat.vault.api.controllers;

import com.uboat.vault.api.services.ImagesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImagesController {
    private final ImagesService imagesService;

    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    @GetMapping(value = "/images/getDefaultProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getDefaultProfilePicture() {
        var bytes = imagesService.getDefaultProfilePicture();
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/images/getSailorProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getSailorProfilePicture(@RequestParam(name = "sailorId") String sailorId) {
        var bytes = imagesService.getSailorProfilePicture(sailorId);
        return ResponseEntity.ok(bytes);
    }

    @GetMapping(value = "/images/getSailorBoatImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<List<byte[]>> getSailorBoatImages(@RequestParam(name = "sailorId") String sailorId) {
        var imagesBytesList = imagesService.getSailorBoatImages(sailorId);
        return ResponseEntity.ok(imagesBytesList);
    }
}
