package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.RegistrationData;
import com.example.uboatvault.api.services.EncryptionService;
import com.example.uboatvault.api.services.RegistrationService;
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

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping(value = "/api/test")
    public String test() {
        return "Running...";
    }

    @PostMapping(value = "/api/checkDeviceRegistration", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> checkDeviceRegistration(@CookieValue(name = "token", required = false) String token,
                                                          @RequestBody RegistrationData registrationData) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/checkDeviceRegistration", registrationData));

        if (token != null) {
            token = EncryptionService.decryptString(token);
            if (token.isEmpty())
                return new ResponseEntity<>("Invalid token.", HttpStatus.BAD_REQUEST);

            String extractedToken = registrationService.searchForToken(registrationData, token);
            if (extractedToken != null)
                return new ResponseEntity<>(EncryptionService.encryptString(extractedToken), HttpStatus.OK);
        }
        String newToken = registrationService.searchForTokenBasedOnRegistrationData(registrationData);
        if (newToken != null)
            return new ResponseEntity<>(EncryptionService.encryptString(newToken), HttpStatus.OK);
        else
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/api/requestRegistration", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> requestRegistration(@RequestBody RegistrationData registrationData) {
        String token = registrationService.requestRegistration(registrationData);
        if (token != null)
            return new ResponseEntity<>(EncryptionService.encryptString(token), HttpStatus.OK);
        else
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/api/register", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> register(@CookieValue(name = "token") String token,
                                           @RequestBody RegistrationData registrationData) {
        token = EncryptionService.decryptString(token);
        if (token.isEmpty())
            return new ResponseEntity<>("Invalid token.", HttpStatus.BAD_REQUEST);

        String extractedValue = registrationService.register(registrationData, token);
        if (extractedValue != null)
            return new ResponseEntity<>(EncryptionService.encryptString(extractedValue), HttpStatus.OK);
        else
            return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
    }
}
