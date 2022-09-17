package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.requests.JourneyRequest;
import com.uboat.vault.api.model.http.requests.SailorConnectionRequest;
import com.uboat.vault.api.model.http.response.JourneyConnectionResponse;
import com.uboat.vault.api.model.http.response.JourneyResponse;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.services.AccountsService;
import com.uboat.vault.api.services.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("api/client")
public class JourneyController {
    private final Logger log = LoggerFactory.getLogger(JourneyController.class);

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The journeys have been retrieved successfully", content = @Content(mediaType = "application/json")),
    })
    @GetMapping(value = "/getMostRecentRides")
    public ResponseEntity<UBoatResponse> getMostRecentRides(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                            @RequestParam @Min(1) @Max(3) Integer ridesRequested) {
        var uBoatResponse = journeyService.getMostRecentRides(authorizationHeader, ridesRequested);

        if (uBoatResponse.getHeader() == UBoatStatus.MOST_RECENT_RIDES_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @GetMapping(value = "/getJourneyBoat")
    public ResponseEntity<Boat> getBoat(@RequestParam(name = "sailorId") String sailorId) {
        var boat = accountsService.getJourneyBoat(sailorId);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/requestJourney")
    public ResponseEntity<JourneyResponse> requestJourney(@RequestBody JourneyRequest request) {
        var response = journeyService.requestJourney(request);
        if (response == null) {
            log.warn("User is not authorised to request a journey.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/connectToSailor")
    public ResponseEntity<JourneyConnectionResponse> connectToSailor(@RequestBody SailorConnectionRequest request) {
        var response = journeyService.connectToSailor(request);
        if (response == null || response.getMsg() == null) {
            log.warn("User is not authorised to connect to the sailor.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        var backendResponse = JourneyConnectionResponse.builder().status(response).message(response.getMsg()).build();

        return new ResponseEntity<>(backendResponse, HttpStatus.OK);
    }
}
