package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestAccount;
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
@RequestMapping("api")
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
    @PostMapping(value = "/checkDeviceRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> checkDeviceRegistration(@RequestBody RequestRegistrationData registrationData) {
        var status = entityService.checkDeviceRegistration(registrationData);

        if (status != UBoatStatus.DEVICE_NOT_REGISTERED)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UBoatResponse(status, true));

        return ResponseEntity.status(HttpStatus.OK).body(new UBoatResponse(status, false));
    }

    @Operation(summary = "Checks if the Json Web Token in the request header is valid. The 'Authorization' header must contains a valid JWT URL encoded as string proceeded by the 'Bearer ' value.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The JWT was extracted and validated. The account exists and the JWT is not expired.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Either the Authorization header does not contain 'Bearer ' or it contains it but the format is not valid. Check response body custom header for more details.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "The JWT has been extracted successfully but it's not valid anymore. Either it can't be decrypted, credentials are missing or it has expired.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/verifyJwt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> verifyJwt(@RequestHeader(value = "Authorization") String authorizationHeader) {
        if (!authorizationHeader.contains("Bearer "))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatResponse(UBoatStatus.MISSING_BEARER, false));

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatResponse(UBoatStatus.INVALID_BEARER_FORMAT, false));

        var jwt = split[1];
        var isValid = jwtService.validateJsonWebToken(jwt);

        if (!isValid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UBoatResponse(UBoatStatus.JWT_INVALID, false));

        return ResponseEntity.status(HttpStatus.OK).body(new UBoatResponse(UBoatStatus.JWT_VALID, true));
    }

    @Operation(summary = "Check if the device from the request is already used or not. The API will search if any of the deviceInfo or the sim cards details present in the request are already present in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A registration token will be returned in the request custom body. The account may have already requested a registration for the current device before. Check response body custom header for more details.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "The credentials are already used by an existing account.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "An exception occurred during the request registration workflow.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> requestRegistration(@RequestBody RequestAccount account) {
        var uBoatResponse = authenticationService.requestRegistration(account);

        return switch (uBoatResponse.getHeader()) {
            case ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(uBoatResponse);

            case VAULT_INTERNAL_SERVER_ERROR ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);

            default -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
        };
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody Account account) {
        String jsonWebToken = authenticationService.login(account);
        if (jsonWebToken == null)
            return ResponseEntity.ok().body(null);
        return ResponseEntity.ok().body(jsonWebToken);
    }
}
