package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.ImagesService;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("images")
@RequiredArgsConstructor
public class ImagesController {
    private final ImagesService imagesService;

    @GetMapping(value = "/defaultProfilePicture", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ResponseEntity<byte[]> getDefaultProfilePicture() {
        var uBoatResponse = imagesService.getDefaultProfilePicture();

        if (uBoatResponse.getHeader() == UBoatStatus.DEFAULT_PROFILE_PICTURE_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body((byte[]) uBoatResponse.getBody());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping(value = "/profileImage", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ResponseEntity<byte[]> getProfileImage(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var uBoatResponse = imagesService.getProfilePicture(authorizationHeader);

        if (uBoatResponse.getHeader() == UBoatStatus.PROFILE_PICTURE_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body((byte[]) uBoatResponse.getBody());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }


    // @ApiResponse(responseCode = "415", description = "The format of the image is not supported. Only png and jpeg are accepted.", content = @Content(mediaType = "application/json")),
    @PostMapping(value = "/profileImage", consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    public @ResponseBody
    ResponseEntity<UBoatDTO> uploadProfileImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody byte[] imageBytes) {
        var uBoatResponse = imagesService.uploadProfileImage(authorizationHeader, imageBytes);

        return switch (uBoatResponse.getHeader()) {
            case PROFILE_IMAGE_UPLOADED, PROFILE_IMAGE_ALREADY_EXISTING ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }

    // @ApiResponse(responseCode = "200", description = "Body will contain the hash of the image", content = @Content(mediaType = "application/json")),
    // @ApiResponse(responseCode = "415", description = "The format of the image is not supported. Only png and jpeg are accepted.", content = @Content(mediaType = "application/json")),
    @PostMapping(value = "/boatImage", consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    public @ResponseBody
    ResponseEntity<UBoatDTO> uploadBoatImage(@RequestHeader(value = "Authorization") String authorizationHeader,
                                             @RequestHeader(value = "Content-Type") String contentType,
                                             @RequestBody byte[] imageBytes) {
        var uBoatResponse = imagesService.uploadBoatImage(authorizationHeader, imageBytes, contentType);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGE_UPLOADED, BOAT_IMAGE_ALREADY_EXISTING ->
                    ResponseEntity.status(HttpStatus.CREATED).body(uBoatResponse);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }

    @GetMapping(value = "/sailorProfilePicture", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE})
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

    @Operation(summary = "This API retrieves the hashes of the images for the sailorId provided in the request parameters. " +
            "The hashes can then be used to retrieve the images by using the /getBoatImage API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat images hashes have been retrieved.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "There is no sailor with the given sailorID as request parameter.", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/boatImagesIdentifiers")
    public @ResponseBody
    ResponseEntity<UBoatDTO> getBoatImagesIdentifiers(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam(required = false) String sailorId) {
        var uBoatResponse = imagesService.getBoatImagesIdentifiers(authorizationHeader, sailorId);

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
    @GetMapping(value = "/boatImage", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    ResponseEntity<byte[]> getBoatImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String identifier) {
        var uBoatResponse = imagesService.getBoatImage(authorizationHeader, identifier);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGE_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body((byte[]) uBoatResponse.getBody());
            case BOAT_IMAGE_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API retrieves deletes the boat image by hash of the account extracted from the JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat image was deleted.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "There is not boat image with the given hash bound to the sailor account.", content = @Content(mediaType = "application/json")),
    })
    @DeleteMapping(value = "/boatImage")
    public @ResponseBody
    ResponseEntity<UBoatDTO> deleteBoatImage(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String identifier) {
        var uBoatResponse = imagesService.deleteBoatImage(authorizationHeader, identifier);

        return switch (uBoatResponse.getHeader()) {
            case BOAT_IMAGE_DELETED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case BOAT_IMAGE_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
