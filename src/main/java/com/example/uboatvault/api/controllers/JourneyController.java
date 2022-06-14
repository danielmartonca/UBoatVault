package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import com.example.uboatvault.api.model.requests.JourneyRequest;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.model.requests.SailorConnectionRequest;
import com.example.uboatvault.api.model.response.JourneyResponse;
import com.example.uboatvault.api.model.response.MostRecentRidesResponse;
import com.example.uboatvault.api.model.response.JourneyConnectionResponse;
import com.example.uboatvault.api.services.AccountsService;
import com.example.uboatvault.api.services.JourneyService;
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
    public ResponseEntity<MostRecentRidesResponse> getMostRecentRides(@CookieValue(name = "token") String token,
                                                                      @RequestBody MostRecentRidesRequest request) {
        var journeys = journeyService.getMostRecentRides(token, request);

        if (journeys == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        MostRecentRidesResponse response = new MostRecentRidesResponse(journeys);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/getJourneyBoat")
    public ResponseEntity<Boat> getBoat(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        var boat = accountsService.getBoat(token, sailorId);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/api/requestJourney")
    public ResponseEntity<JourneyResponse> requestJourney(@CookieValue(name = "token") String token,
                                                          @RequestBody JourneyRequest request) {

        var response = journeyService.requestJourney(token, request);
        if (response == null) {
            log.warn("User is not authorised to request a journey.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/api/connectToSailor")
    public ResponseEntity<JourneyConnectionResponse> connectToSailor(@CookieValue(name = "token") String token,
                                                                     @RequestBody SailorConnectionRequest request) {

        var response = journeyService.connectToSailor(token, request);
        if (response == null || response.getMsg() == null) {
            log.warn("User is not authorised to connect to the sailor.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        var backendResponse = JourneyConnectionResponse.builder()
                .status(response)
                .message(response.getMsg())
                .build();

        return new ResponseEntity<>(backendResponse, HttpStatus.OK);
    }
}
