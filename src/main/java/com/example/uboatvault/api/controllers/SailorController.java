package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.sailing.Journey;
import com.example.uboatvault.api.model.requests.PulseRequest;
import com.example.uboatvault.api.model.requests.SailorConnectionRequest;
import com.example.uboatvault.api.model.requests.UpdateBoatRequest;
import com.example.uboatvault.api.model.response.SailorConnectionResponse;
import com.example.uboatvault.api.services.AccountsService;
import com.example.uboatvault.api.services.JourneyService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SailorController {
    private final Logger log = LoggerFactory.getLogger(SailorController.class);

    private final JourneyService journeyService;

    @Autowired
    public SailorController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @PostMapping(value = "/api/pulse")
    public ResponseEntity<Boolean> pulse(@CookieValue(name = "token") String token, @RequestBody PulseRequest request) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/pulse", request));

        var hasProcessed = journeyService.pulse(token, request);
        if (hasProcessed == null) return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);

        if (hasProcessed) {
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/pulse"), "true");
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/api/findClients")
    public ResponseEntity<List<Journey>> findClients(@CookieValue(name = "token") String token,
                                                     @RequestBody Account request) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/findClients", request));

        var response = journeyService.findClients(token, request);
        if (response == null) {
            log.warn("User is not authorised to find clients.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/findClients", response));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
