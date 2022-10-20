package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.dto.JourneyDTO;
import com.uboat.vault.api.model.dto.PulseDTO;
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
        if (uBoatResponse.getHeader() == UBoatStatus.PULSE_SUCCESSFUL)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @Operation(summary = "Queries the database for journeys that have the status CLIENT_ACCEPTED and the sailor id the current sailor's id extracted from JWT. " +
            "When the users call the API /connectToSailor, a new Journey is created with status CLIENT_ACCEPTED and the sailor id of the chosen sailor. " +
            "Then the sailor has to call the /selectClient API in order to proceed with the Journey.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. A list of available journeys is returned if existing, null otherwise.", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/findClients")
    public ResponseEntity<UBoatDTO> findClients(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var uBoatResponse = journeyService.findClients(authorizationHeader);

        return switch (uBoatResponse.getHeader()) {
            case CLIENTS_FOUND, NO_CLIENTS_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "Queries the database for journeys that have the status CLIENT_ACCEPTED and the sailor id the current sailor's id extracted from JWT. " +
            "This API is called by the sailor only after he has called /findClients to query all available journeys.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. True will be returned if a journey was selected by the client " +
                    "and all the other requested journeys are canceled or false if journey from the request could not be found in the database.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/selectClient")
    public ResponseEntity<UBoatDTO> selectClient(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody JourneyDTO journey) {

        var uBoatResponse = journeyService.selectClient(authorizationHeader, journey);
        return switch (uBoatResponse.getHeader()) {
            case JOURNEY_SELECTED, JOURNEY_NOT_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
