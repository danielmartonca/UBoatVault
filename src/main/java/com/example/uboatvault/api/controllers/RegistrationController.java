package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.RegistrationData;
import com.example.uboatvault.api.model.requests.RegistrationRequest;
import com.example.uboatvault.api.model.response.RegistrationDataResponse;
import com.example.uboatvault.api.services.EncryptionService;
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

@RestController
public class RegistrationController {
    Logger log = LoggerFactory.getLogger(RegistrationController.class);

    RegistrationService registrationService;
    EncryptionService encryptionService;
    TokenService tokenService;

    @Autowired
    public RegistrationController(RegistrationService registrationService, EncryptionService encryptionService, TokenService tokenService) {
        this.registrationService = registrationService;
        this.encryptionService = encryptionService;
        this.tokenService = tokenService;
    }

    @GetMapping(value = "/api/test")
    public String test() {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.GET, "/api/test", null));
        return "Running...";
    }

    @PostMapping(value = "/api/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> checkDeviceRegistration(@CookieValue(name = "token", required = false) String token,
                                                                            @RequestBody RegistrationData registrationData) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/checkDeviceRegistration", registrationData));

        if (token != null) {
            if (!tokenService.isTokenDecryptable(token)) {
                log.error("Token is not decryptable.");
                return new ResponseEntity<>(new RegistrationDataResponse(null, null), HttpStatus.NOT_ACCEPTABLE);
            }

            String extractedToken = registrationService.searchForToken(registrationData, token);
            if (extractedToken != null)
                return new ResponseEntity<>(new RegistrationDataResponse(true, extractedToken), HttpStatus.OK);
        }

        String dbToken = registrationService.searchForTokenBasedOnRegistrationData(registrationData);
        if (dbToken != null)
            return new ResponseEntity<>(new RegistrationDataResponse(true, dbToken), HttpStatus.OK);
        else
            return new ResponseEntity<>(new RegistrationDataResponse(false, null), HttpStatus.OK);
    }

    @GetMapping(value = "/api/checkUsername")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.GET, "/api/checkUsername/username='" + username+"'", null));

        if (!registrationService.isUsernameUsed(username))
            return new ResponseEntity<>(true,HttpStatus.OK);
        else
            return new ResponseEntity<>(false,HttpStatus.OK);//case if username already exists
    }

    @PostMapping(value = "/api/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> requestRegistration(@RequestBody RegistrationData registrationData) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/requestRegistration", registrationData));
        String token = registrationService.requestRegistration(registrationData);
        RegistrationDataResponse registrationResponse;
        if (token != null)
            registrationResponse = new RegistrationDataResponse(false, token);
        else
            registrationResponse = new RegistrationDataResponse(false, null);

        return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/api/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> register(@CookieValue(name = "token") String token,
                                                             @RequestBody RegistrationRequest registrationRequest) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/register", registrationRequest));

        if (!tokenService.isTokenDecryptable(token)) {
            log.error("Token is not decryptable.");
            return new ResponseEntity<>(new RegistrationDataResponse(null, null), HttpStatus.BAD_REQUEST);
        }

        String extractedValue = registrationService.register(registrationRequest, token);
        if (extractedValue != null)
            return new ResponseEntity<>(new RegistrationDataResponse(true, extractedValue), HttpStatus.OK);
        else
            return new ResponseEntity<>(new RegistrationDataResponse(false, null), HttpStatus.NOT_ACCEPTABLE);
    }
}
