package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.AccountsService;
import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.dto.JourneyDTO;
import com.uboat.vault.api.model.dto.JourneyRequestDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/client")
@RequiredArgsConstructor
public class UBoatClientController {
    private final JourneyService journeyService;
    private final AccountsService accountsService;

    @Operation(summary = "Retrieves information about the boat of the sailor searched by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat details have been retrieved.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "The sailor could not be found by that id.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/sailorBoat")
    public ResponseEntity<UBoatDTO> getSailorBoat(@RequestParam(name = "sailorId") String sailorId) {
        var uBoatResponse = accountsService.getSailorBoat(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_BOAT_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API creates Journies which are possible to be selected by the client and the sailor. " +
            "It fetches all active sailors, calls all services to calculate details about the journey and creates them in status INITIATED for each free active sailor found withing the limit distance that has a possible route between him, the client and the destination. " +
            "Connection to the sailor can only be established by calling /chooseJourney API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details about each journey are returned or empty list if no free sailors could be detected.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/requestJourney")
    public ResponseEntity<UBoatDTO> requestJourney(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody JourneyRequestDTO request) {
        var uBoatResponse = journeyService.requestJourney(authorizationHeader, request);

        return switch (uBoatResponse.getHeader()) {
            case JOURNEYS_INITIATED, NO_FREE_SAILORS_FOUND, PAYMENT_METHOD_NOT_FOUND ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API deletes all journeys in state INITIATED for the client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All journeys of the client in state INITIATED have been deleted.", content = @Content(mediaType = "application/json")),
    })
    @DeleteMapping(value = "/deleteInitiatedJourneys")
    public ResponseEntity<UBoatDTO> deleteInitiatedJourneys(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var uBoatResponse = journeyService.deleteInitiatedJourneys(authorizationHeader);

        if (uBoatResponse.getHeader() == UBoatStatus.INITIATED_JOURNIES_DELETED)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Operation(summary = "This method establishes the connection between the client and the sailor after the client has chosen the sailor's journey sent in the request body." +
            "It validates that the data in the request (the sailor is online, and it is not already having another journey) then puts the Journey in stage CLIENT_ACCEPTED, dismissing all the other journeys he has in stage INITIATED into CLIENT_CANCELED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new journey was created and the other ones with status CLIENT_ACCEPTED(due to app restart/error etc) are canceled.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Could not find the sailor by sailor ID.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/chooseJourney")
    public ResponseEntity<UBoatDTO> chooseJourney(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody JourneyDTO request) {
        var uBoatResponse = journeyService.chooseJourney(authorizationHeader, request);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_NOT_FOUND, PAYMENT_METHOD_NOT_FOUND, JOURNEY_FOR_SAILOR_NOT_FOUND, CLIENT_ACCEPTED_JOURNEY ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "Called by the clients to check if the sailor has accepted the Journey. A Journey is accepted if it is in state SAILOR_ACCEPTED." +
            "This API also changes the journey (if found) state to SAILING_TO_CLIENT.")
    @GetMapping(value = "/sailorAccepted")
    public ResponseEntity<UBoatDTO> sailorAccepted(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam Long sailorId) {
        var uBoatResponse = journeyService.hasSailorAcceptedJourney(authorizationHeader, sailorId);

        return switch (uBoatResponse.getHeader()) {
            case JOURNEY_WITH_STATE_NOT_FOUND, JOURNEY_WITH_STATE_FOUND ->
                    ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
