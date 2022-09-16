package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.services.ImagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @PutMapping(value = "/uploadProfileImage")
    public @ResponseBody
    ResponseEntity<UBoatResponse> uploadProfileImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody byte[] imageBytes) {
        var uBoatResponse = imagesService.uploadProfileImage(authorizationHeader, imageBytes);

        return switch (uBoatResponse.getHeader()) {
            case PROFILE_IMAGE_UPLOADED, PROFILE_IMAGE_ALREADY_EXISTING ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
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

    @Operation(summary = "This API retrieves the hashes of the images for the sailorId provided in the request parameters. " +
            "The hashes can then be used to retrieve the images by using the /getBoatImage API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat images hashes have been retrieved.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "There is no sailor with the given sailorID as request parameter.", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/getBoatImagesIdentifiers")
    public @ResponseBody
    ResponseEntity<UBoatResponse> getBoatImagesIdentifiers(@RequestParam String sailorId) {
        var uBoatResponse = imagesService.getBoatImagesIdentifiers(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGES_HASHES_EMPTY, BOAT_IMAGES_HASHES_RETRIEVED ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }

    @Operation(summary = "This API retrieves the boat image searched by the image hash. It has an internal workflow to disallow sailors to access other sailor resources. " +
            "Clients can access any sailor boat images but sailors can only access their own boat images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The image bytes have been retrieved successfully.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No image matching the request parameter hash was found for any sailor(if client accesses the api)" +
                    " or personal boat(if sailor accesses the API).", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/getBoatImage")
    public @ResponseBody
    ResponseEntity<Object> getBoatImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String identifier) {
        var uBoatResponse = imagesService.getBoatImage(authorizationHeader, identifier);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGE_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse.getBody());
            case BOAT_IMAGE_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
