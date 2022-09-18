package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.RequestNewJourney;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.services.AccountsService;
import com.uboat.vault.api.services.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("api/client")
public class JourneyController {
    private final JourneyService journeyService;
    private final AccountsService accountsService;

    @Autowired
    public JourneyController(JourneyService journeyService, AccountsService accountsService) {
        this.journeyService = journeyService;
        this.accountsService = accountsService;
    }

    //TODO delete this after it's use cases are gone
    @GetMapping(value = "/test/addFakeJourney")
    public ResponseEntity<Boolean> addFakeJourney(@RequestParam String clientId, @RequestParam String sailorId) {
        return new ResponseEntity<>(journeyService.addFakeJourney(clientId, sailorId), HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieves the last {ridesRequested} number of journeys for the client extracted from the JWT. ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The journeys have been retrieved successfully", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/getMostRecentRides")
    public ResponseEntity<UBoatResponse> getMostRecentRides(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam @Min(1) @Max(3) Integer ridesRequested) {
        var uBoatResponse = journeyService.getMostRecentRides(authorizationHeader, ridesRequested);

        if (uBoatResponse.getHeader() == UBoatStatus.MOST_RECENT_RIDES_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @Operation(summary = "Retrieves information about the boat of the sailor searched by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat details have been retrieved.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "The sailor could not be found by that id.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/getSailorBoat")
    public ResponseEntity<UBoatResponse> getSailorBoat(@RequestParam(name = "sailorId") String sailorId) {
        var uBoatResponse = accountsService.getSailorBoat(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_BOAT_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API retrieves information about possible journeys." +
            " It fetches all active sailors and calls all services to calculate details about the journey such as: " +
            "sailor id, sailor name, estimated cost, duration, quality etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Free sailors were found. Details about each journey are returned.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No free sailors could be found.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/requestJourney")
    public ResponseEntity<UBoatResponse> requestJourney(@RequestBody RequestNewJourney request) {
        var uBoatResponse = journeyService.requestJourney(request);

        return switch (uBoatResponse.getHeader()) {
            case FREE_SAILORS_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case NO_FREE_SAILORS_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

//    @PostMapping(value = "/connectToSailor")
//    public ResponseEntity<JourneyConnectionResponse> connectToSailor(@RequestBody SailorConnectionRequest request) {
//        var response = journeyService.connectToSailor(request);
//        if (response == null || response.getMsg() == null) {
//            log.warn("User is not authorised to connect to the sailor.");
//            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
//        }
//
//        var backendResponse = JourneyConnectionResponse.builder().status(response).message(response.getMsg()).build();
//
//        return new ResponseEntity<>(backendResponse, HttpStatus.OK);
//    }
}
