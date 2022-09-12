package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestRegistrationData;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.services.AuthenticationService;
import com.uboat.vault.api.services.EntityService;
import com.uboat.vault.api.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Check if the device from the request is already used or not. The API will search if any of the deviceInfo or the sim cards details present in the request are already present in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Neither device info nor any sim card are present in the database.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Either the sim card or device info is already used. Check response body custom header for more details.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/api/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> checkDeviceRegistration(@RequestBody RequestRegistrationData registrationData) {
        var status = entityService.checkDeviceRegistration(registrationData);

        if (status != UBoatStatus.DEVICE_NOT_REGISTERED)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UBoatResponse(status, true));

        return ResponseEntity.status(HttpStatus.OK).body(new UBoatResponse(status, false));
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
