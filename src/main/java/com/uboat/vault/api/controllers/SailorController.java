package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestJourney;
import com.uboat.vault.api.model.http.new_requests.RequestPulse;
import com.uboat.vault.api.services.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/sailor")
public class SailorController {
    private final JourneyService journeyService;

    @Autowired
    public SailorController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @Operation(summary = "Records the location of the sailor calling it and updates its status to looking for clients if specified in the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The last known location of the sailor was updated and the status was saved.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/pulse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> pulse(@RequestHeader(value = "Authorization") String authorizationHeader,
                                               @RequestBody RequestPulse pulse) {
        var uBoatResponse = journeyService.pulse(authorizationHeader, pulse);
        if (uBoatResponse.getHeader() == UBoatStatus.PULSE_SUCCESSFUL)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @Operation(summary = "Queries the database for journeys that have the status ESTABLISHING_CONNECTION and the sailor id the current sailor's id extracted from JWT. " +
            "When the users call the API /connectToSailor, a new Journey is created with status ESTABLISHING_CONNECTION and the sailor id of the chosen sailor. " +
            "Then the sailor has to call the /selectClient API in order to proceed with the Journey.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. A list of available journeys is returned if existing, null otherwise.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/findClients")
    public ResponseEntity<UBoatResponse> findClients(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var uBoatResponse = journeyService.findClients(authorizationHeader);

        return switch (uBoatResponse.getHeader()) {
            case CLIENTS_FOUND, NO_CLIENTS_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "Queries the database for journeys that have the status ESTABLISHING_CONNECTION and the sailor id the current sailor's id extracted from JWT. " +
            "This API is called by the sailor only after he has called /findClients to query all available journeys.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Api was processed correctly. True will be returned if a journey was selected by the client and all the other requested journeys are canceled " +
                    "or false if journey from the request could not be found in the database.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/selectClient")
    public ResponseEntity<UBoatResponse> selectClient(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody RequestJourney journey) {

        var uBoatResponse = journeyService.selectClient(authorizationHeader, journey);
        return switch (uBoatResponse.getHeader()) {
            case JOURNEY_SELECTED, JOURNEY_NOT_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
