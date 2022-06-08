package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.requests.JourneyRequest;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.model.response.JourneyResponse;
import com.example.uboatvault.api.model.response.MostRecentRidesResponse;
import com.example.uboatvault.api.services.JourneyService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class JourneyController {
    private final Logger log = LoggerFactory.getLogger(JourneyController.class);

    private final JourneyService journeyService;

    @Autowired
    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    //TODO delete this after it's use cases are gone
    @GetMapping(value = "/api/test/addFakeJourney")
    public ResponseEntity<Boolean> addFakeJourney(@RequestParam String clientId, @RequestParam String sailorId) {
        journeyService.addFakeJourney(clientId, sailorId);
        return new ResponseEntity<>(true, HttpStatus.CREATED);
    }


    @PostMapping(value = "/api/getMostRecentRides")
    public ResponseEntity<MostRecentRidesResponse> getMostRecentRides(@CookieValue(name = "token") String token,
                                                                      @RequestBody MostRecentRidesRequest request) {

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getMostRecentRides", request));

        var journeys = journeyService.getMostRecentRides(token, request);

        if (journeys == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        MostRecentRidesResponse response = new MostRecentRidesResponse(journeys);
        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getMostRecentRides", response));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/api/requestJourney")
    public ResponseEntity<JourneyResponse> requestJourney(@CookieValue(name = "token") String token,
                                                          @RequestBody JourneyRequest request) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/requestJourney", request));

        var response = journeyService.requestJourney(token, request);
        if (response == null) {
            log.warn("User is not authorised to request a journey.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/requestJourney", response));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
