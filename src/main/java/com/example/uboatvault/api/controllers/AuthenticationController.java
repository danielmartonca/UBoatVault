package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.info.RegistrationData;
import com.example.uboatvault.api.services.AuthenticationService;
import com.example.uboatvault.api.services.EntityService;
import com.example.uboatvault.api.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AuthenticationController {
    private final EntityService entityService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @Autowired
    public AuthenticationController(EntityService entityService, AuthenticationService authenticationService, JwtService jwtService) {
        this.entityService = entityService;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/api/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> checkDeviceRegistration(@RequestBody RegistrationData registrationData) {
        var isDeviceRegistered = entityService.checkDeviceRegistration(registrationData);
        return ResponseEntity.ok(isDeviceRegistered);
    }

    @PostMapping(value = "/api/verifyJsonWebToken", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> verifyJsonWebToken(@RequestHeader(value = "Authorization") String authorizationHeader) {
        if (!authorizationHeader.contains("Bearer "))
            return ResponseEntity.badRequest().body(false);

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            return ResponseEntity.badRequest().body(null);

        var jwt = split[1];
        var isValid = jwtService.validateJsonWebToken(jwt);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping(value = "/api/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> requestRegistration(@RequestBody Account account) {
        var registrationToken = authenticationService.requestRegistration(account);
        return ResponseEntity.ok(registrationToken);
    }

    @PostMapping(value = "/api/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> register(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody Account account) {
        if (!authorizationHeader.contains("RToken "))
            return ResponseEntity.badRequest().body(null);

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            return ResponseEntity.badRequest().body(null);
        var registrationToken = split[1];

        if (registrationToken.isEmpty())
            return ResponseEntity.badRequest().body(null);

        var jsonWebToken = authenticationService.register(account, registrationToken);
        return ResponseEntity.ok(jsonWebToken);
    }

    @PostMapping(value = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody Account account) {
        String jsonWebToken = authenticationService.login(account);
        if (jsonWebToken == null)
            return ResponseEntity.ok().body(null);
        return ResponseEntity.ok().body(jsonWebToken);
    }
}
