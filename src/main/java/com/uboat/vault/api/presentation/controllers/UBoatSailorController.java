package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.dto.JourneyDTO;
import com.uboat.vault.api.model.dto.PulseDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
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
@RequestMapping("api/sailor")
@RequiredArgsConstructor
public class UBoatSailorController {
    private final JourneyService journeyService;

    @Operation(summary = "Records the location of the sailor calling it and updates its status to looking for clients if specified in the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The last known location of the sailor was updated and the status was saved.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/pulse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatDTO> pulse(@RequestHeader(value = "Authorization") String authorizationHeader,
                                          @RequestBody PulseDTO pulse) {
        var uBoatResponse = journeyService.pulse(authorizationHeader, pulse);

        return switch (uBoatResponse.getHeader()) {
            case PULSE_SUCCESSFUL, PULSE_JOURNEY_DETECTED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API is called by sailors to find possible new journey. After the clients have created journeys by calling /requestJourney and /chooseJourney APIs," +
            " the sailor can call this API to retrieve the closest journey matching this criteria. However, in order to establish a connection between him and the client, the sailor also has to call /selectClient with the data retrieved using this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. The closest journey to between the sailor and the client if existing, null otherwise.", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/findClient")
    public ResponseEntity<UBoatDTO> findClient(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var uBoatResponse = journeyService.findClient(authorizationHeader);

        return switch (uBoatResponse.getHeader()) {
            case CLIENT_FOUND, NO_CLIENT_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API establishes a Journey between the caller and the Journey entity. The request journey must be fetched using /findClient API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. True will be returned if a journey was selected by the client " +
                    "and all the other requested journeys are canceled or false if journey from the request could not be found in the database.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/confirmClient")
    public ResponseEntity<UBoatDTO> confirmClient(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody JourneyDTO journey) {

        var uBoatResponse = journeyService.confirmClient(authorizationHeader, journey);
        return switch (uBoatResponse.getHeader()) {
            case JOURNEY_CONFIRMED, JOURNEY_NOT_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
