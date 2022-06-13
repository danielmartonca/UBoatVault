package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import com.example.uboatvault.api.model.requests.JourneyRequest;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
import com.example.uboatvault.api.model.requests.SailorConnectionRequest;
import com.example.uboatvault.api.model.response.JourneyResponse;
import com.example.uboatvault.api.model.response.MostRecentRidesResponse;
import com.example.uboatvault.api.model.response.SailorConnectionResponse;
import com.example.uboatvault.api.services.AccountsService;
import com.example.uboatvault.api.services.JourneyService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import com.example.uboatvault.api.utility.logging.TextColor;
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

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getMostRecentRides", request));

        var journeys = journeyService.getMostRecentRides(token, request);

        if (journeys == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        MostRecentRidesResponse response = new MostRecentRidesResponse(journeys);
        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getMostRecentRides", response));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/getJourneyBoat")
    public ResponseEntity<Boat> getBoat(@CookieValue(name = "token") String token, @RequestParam(name = "sailorId") String sailorId) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getJourneyBoat?sailorId=" + sailorId));

        var boat = accountsService.getBoat(token, sailorId);
        if (boat != null) {
            log.info(LoggingUtils.colorString("Boat sent back to the client.", TextColor.PURPLE));

            var images = boat.getBoatImages();
            boat.setBoatImages(null);
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getJourneyBoat?sailorId=" + sailorId, boat));
            if (images != null && !images.isEmpty())
                log.info(LoggingUtils.colorString("Request body had " + images.size() + " images as bytes so they were not logged.", TextColor.PURPLE));
            boat.setBoatImages(images);
            return new ResponseEntity<>(boat, HttpStatus.OK);
        } else {
            log.info("Null sent back to the client.");
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getJourneyBoat?sailorId=" + sailorId));
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
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

    @PostMapping(value = "/api/connectToSailor")
    public ResponseEntity<SailorConnectionResponse> connectToSailor(@CookieValue(name = "token") String token,
                                                                    @RequestBody SailorConnectionRequest request) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/connectToSailor", request));

        var possibleResponse = journeyService.connectToSailor(token, request);
        if (possibleResponse == null || possibleResponse.getMsg() == null) {
            log.warn("User is not authorised to connect to the sailor.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        var backendResponse = SailorConnectionResponse.builder()
                .message(possibleResponse.getMsg())
                .build();

        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/connectToSailor", possibleResponse));
        return new ResponseEntity<>(backendResponse, HttpStatus.OK);
    }
}
