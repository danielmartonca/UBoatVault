package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.RegistrationData;
import com.example.uboatvault.api.model.requests.RegistrationRequest;
import com.example.uboatvault.api.model.response.RegistrationDataResponse;
import com.example.uboatvault.api.services.CookiesService;
import com.example.uboatvault.api.services.EncryptionService;
import com.example.uboatvault.api.services.RegistrationService;
import com.example.uboatvault.api.services.TokenService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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

    RegistrationService registrationService;
    EncryptionService encryptionService;
    TokenService tokenService;
    CookiesService cookiesService;

    @Autowired
    public RegistrationController(RegistrationService registrationService, EncryptionService encryptionService, TokenService tokenService, CookiesService cookiesService) {
        this.registrationService = registrationService;
        this.encryptionService = encryptionService;
        this.tokenService = tokenService;
        this.cookiesService = cookiesService;
    }

    @PostMapping(value = "/api/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> checkDeviceRegistration(@CookieValue(name = "token", required = false) String token,
                                                                            @RequestBody RegistrationData registrationData,
                                                                            HttpServletResponse response) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/checkDeviceRegistration", registrationData));

        if (token != null) {
            if (!tokenService.isTokenDecryptable(token)) {
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
            log.info("No token was found for the given registration data. Device is not registered.");
            return new ResponseEntity<>(new RegistrationDataResponse(false, null), HttpStatus.OK);
        }
    }

    @GetMapping(value = "/api/checkUsername")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.GET, "/api/checkUsername/username='" + username + "'", null));

        if (!registrationService.usernameMatchesPattern(username))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (!registrationService.isUsernameUsed(username))
            return new ResponseEntity<>(true, HttpStatus.OK);
        else
            return new ResponseEntity<>(false, HttpStatus.OK);
    }

    @GetMapping(value = "/api/checkPhoneNumber")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber,
                                                    @RequestParam String dialCode,
                                                    @RequestParam String isoCode) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.GET, "/api/checkPhoneNumber?phoneNumber='" + phoneNumber + "';" + "dialCode='" + dialCode + "';isoCode='" + isoCode + "'", null));

        if (!registrationService.phoneNumberMatchesPattern(phoneNumber) || dialCode.length() > 5 || isoCode.length() >= 3)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (!registrationService.isPhoneNumberUsed(phoneNumber, dialCode, isoCode))
            return new ResponseEntity<>(true, HttpStatus.OK);
        else
            return new ResponseEntity<>(false, HttpStatus.OK);
    }

    @PostMapping(value = "/api/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<RegistrationDataResponse> requestRegistration(@RequestBody Account account, HttpServletResponse response) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/requestRegistration", account));
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
    public ResponseEntity<RegistrationDataResponse> register(@CookieValue(name = "token") String token,
                                                             @RequestBody Account account) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/register", account));

        if (!tokenService.isTokenDecryptable(token)) {
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
