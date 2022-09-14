package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.services.ImagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        var uBoatResponse = imagesService.getDefaultProfilePicture();

        if (uBoatResponse.getHeader() == UBoatStatus.DEFAULT_PROFILE_PICTURE_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body((byte[]) uBoatResponse.getBody());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping(value = "/getSailorProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    ResponseEntity<byte[]> getSailorProfilePicture(@RequestParam(name = "sailorId") String sailorId) {
        var uBoatResponse = imagesService.getSailorProfilePicture(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_PROFILE_PICTURE_RETRIEVED, SAILOR_PROFILE_PICTURE_NOT_SET ->
                    ResponseEntity.status(HttpStatus.OK).body((byte[]) uBoatResponse.getBody());
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @GetMapping(value = "/getSailorBoatImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<Object> getSailorBoatImages(@RequestParam(name = "sailorId") String sailorId) {
        var uBoatResponse = imagesService.getSailorBoatImages(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_BOAT_IMAGES_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse.getBody());
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @PutMapping(value = "/uploadBoatImage")
    public @ResponseBody
    ResponseEntity<UBoatResponse> uploadBoatImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody byte[] imageBytes) {
        var uBoatResponse = imagesService.uploadBoatImage(authorizationHeader, imageBytes);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGE_UPLOADED, BOAT_IMAGE_ALREADY_EXISTING ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }
}
