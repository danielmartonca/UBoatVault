package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.info.RegistrationData;
import com.example.uboatvault.api.model.response.RegistrationDataResponse;
import com.example.uboatvault.api.services.CookiesService;
import com.example.uboatvault.api.services.RegistrationService;
import com.example.uboatvault.api.services.TokenService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RegistrationController {
    private final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationService registrationService;
    private final TokenService tokenService;
    private final CookiesService cookiesService;

    @Autowired
    public RegistrationController(RegistrationService registrationService, TokenService tokenService, CookiesService cookiesService) {
        this.registrationService = registrationService;
        this.tokenService = tokenService;
        this.cookiesService = cookiesService;
    }

    @PostMapping(value = "/api/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> checkDeviceRegistration(@CookieValue(name = "token", required = false) String token, @RequestBody RegistrationData registrationData, HttpServletResponse response) {

        if (token != null) {
            if (tokenService.isTokenInvalid(token)) {
                log.error("Token is not decryptable.");

                return new ResponseEntity<>(new RegistrationDataResponse(null, null), HttpStatus.NOT_ACCEPTABLE);
            }

            String extractedToken = registrationService.searchForTokenByValue(token, registrationData);
            if (extractedToken != null) {
                log.info("Token is valid. Returning it");
                cookiesService.addTokenToSetCookiesHeader(token, response);

                return new ResponseEntity<>(new RegistrationDataResponse(true, extractedToken), HttpStatus.OK);
            }
        }

        String dbToken = registrationService.searchForTokenBasedOnRegistrationData(registrationData);
        if (dbToken != null) {
            log.info("Found token in the database for the given registration data. Device must request it with credentials as well in order to retrieve it.");
            return new ResponseEntity<>(new RegistrationDataResponse(true, null), HttpStatus.OK);
        } else {
            log.info("Token could not be retrieved. Either registration data is not in the database or it is bound to multiple accounts.");
            return new ResponseEntity<>(new RegistrationDataResponse(false, null), HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> requestRegistration(@RequestBody Account account, HttpServletResponse response) {
        String token = registrationService.requestRegistrationToken(account);
        RegistrationDataResponse registrationResponse;
        if (token != null) {
            cookiesService.addTokenToSetCookiesHeader(token, response);
            log.info("Generated new token.");
            registrationResponse = new RegistrationDataResponse(false, token);
        } else {
            log.warn("Device with the given specs is already registered.");
            registrationResponse = new RegistrationDataResponse(false, null);
        }

        return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/api/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> register(@CookieValue(name = "token") String token, @RequestBody Account account) {

        if (tokenService.isTokenInvalid(token)) {
            log.error("Token is not decryptable.");

            return new ResponseEntity<>(new RegistrationDataResponse(null, null), HttpStatus.BAD_REQUEST);
        }

        String extractedValue = registrationService.register(account, token);
        if (extractedValue != null) {
            log.info("Device registered successfully.");

            return new ResponseEntity<>(new RegistrationDataResponse(true, extractedValue), HttpStatus.OK);
        } else {
            log.warn("Device registration failed.");

            return new ResponseEntity<>(new RegistrationDataResponse(false, null), HttpStatus.OK);
        }
    }
}
