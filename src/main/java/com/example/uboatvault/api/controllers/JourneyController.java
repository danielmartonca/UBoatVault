package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.location.Journey;
import com.example.uboatvault.api.model.requests.MostRecentRidesRequest;
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

//    //TODO
//    @PutMapping(value = "/api/beginJourney")
//    public ResponseEntity<Boolean> beginJourney(@CookieValue(name = "token") String token,
//                                                @RequestBody Account account, String sailorToken) {
//
//        return new ResponseEntity<>(false, HttpStatus.CREATED);
//    }
//
//    //TODO
//    @PostMapping(value = "/api/pay")
//    public ResponseEntity<Boolean> pay(@CookieValue(name = "token") String token,
//                                       @RequestBody Account account, String sailorToken) {
//
//        return new ResponseEntity<>(false, HttpStatus.OK);
//    }
//
//    //TODO
//    @PostMapping(value = "/api/endJourney")
//    public ResponseEntity<Boolean> endJourney(@CookieValue(name = "token") String token,
//                                              @RequestBody Account account, String sailorToken) {
//
//        return new ResponseEntity<>(false, HttpStatus.OK);
//    }

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
}
