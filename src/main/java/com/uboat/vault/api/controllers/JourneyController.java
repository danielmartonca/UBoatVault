package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.model.requests.JourneyRequest;
import com.uboat.vault.api.model.requests.MostRecentRidesRequest;
import com.uboat.vault.api.model.requests.SailorConnectionRequest;
import com.uboat.vault.api.model.response.JourneyConnectionResponse;
import com.uboat.vault.api.model.response.JourneyResponse;
import com.uboat.vault.api.model.response.MostRecentRidesResponse;
import com.uboat.vault.api.services.AccountsService;
import com.uboat.vault.api.services.JourneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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
    @GetMapping(value = "/api/test/addFakeJourney")
    public ResponseEntity<Boolean> addFakeJourney(@RequestParam String clientId, @RequestParam String sailorId) {
        return new ResponseEntity<>(journeyService.addFakeJourney(clientId, sailorId), HttpStatus.CREATED);
    }

    @PostMapping(value = "/api/getMostRecentRides")
    public ResponseEntity<MostRecentRidesResponse> getMostRecentRides(@RequestBody MostRecentRidesRequest request) {
        var journeys = journeyService.getMostRecentRides(request);

        if (journeys == null) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        MostRecentRidesResponse response = new MostRecentRidesResponse(journeys);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/getJourneyBoat")
    public ResponseEntity<Boat> getBoat(@RequestParam(name = "sailorId") String sailorId) {
        var boat = accountsService.getBoat(sailorId);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/api/requestJourney")
    public ResponseEntity<JourneyResponse> requestJourney(@RequestBody JourneyRequest request) {
        var response = journeyService.requestJourney(request);
        if (response == null) {
            log.warn("User is not authorised to request a journey.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/api/connectToSailor")
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
