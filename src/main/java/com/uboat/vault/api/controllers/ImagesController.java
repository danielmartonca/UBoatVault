package com.uboat.vault.api.controllers;

import com.uboat.vault.api.services.ImagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("images")
public class ImagesController {
    private final ImagesService imagesService;

    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    @GetMapping(value = "/getDefaultProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getDefaultProfilePicture() {
        var bytes = imagesService.getDefaultProfilePicture();
        return ResponseEntity.status(HttpStatus.OK).body(bytes);
    }

    @GetMapping(value = "/getSailorProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getSailorProfilePicture(@RequestParam(name = "sailorId") String sailorId) {
        var bytes = imagesService.getSailorProfilePicture(sailorId);
        return ResponseEntity.status(HttpStatus.OK).body(bytes);
    }

    @GetMapping(value = "/getSailorBoatImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<List<byte[]>> getSailorBoatImages(@RequestParam(name = "sailorId") String sailorId) {
        var imagesBytesList = imagesService.getSailorBoatImages(sailorId);
        return ResponseEntity.status(HttpStatus.OK).body(imagesBytesList);
    }
}
