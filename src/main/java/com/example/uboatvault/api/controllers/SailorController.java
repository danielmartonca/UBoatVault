package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.requests.PulseRequest;
import com.example.uboatvault.api.services.AccountsService;
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

@RestController
public class SailorController {
    private final Logger log = LoggerFactory.getLogger(SailorController.class);

    private final AccountsService accountsService;

    @Autowired
    public SailorController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(value = "/api/updateBoatDetails")
    public ResponseEntity<Boolean> updateBoatDetails(@CookieValue(name = "token") String token,
                                         @RequestBody PulseRequest request) {

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/updateBoatDetails", request));

        var hasProcessed = accountsService.pulse(token, request.getAccount(), request.getLocationData());
        if (hasProcessed == null)
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);

        if (hasProcessed) {
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/updateBoatDetails"),"true");
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/api/pulse")
    public ResponseEntity<Boolean> pulse(@CookieValue(name = "token") String token,
                                         @RequestBody PulseRequest request) {

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/pulse", request));

        var hasProcessed = accountsService.pulse(token, request.getAccount(), request.getLocationData());
        if (hasProcessed == null)
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);

        if (hasProcessed) {
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/pulse"),"true");
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
}
