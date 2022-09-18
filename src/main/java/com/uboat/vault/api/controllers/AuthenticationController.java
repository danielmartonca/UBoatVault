package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.RequestAccount;
import com.uboat.vault.api.model.http.RequestRegistrationData;
import com.uboat.vault.api.model.http.UBoatResponse;
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

    @Operation(summary = "Request a registration token for the account given in the request body. " +
            "It will query the database if the credentials are already used and return a conflict message if so," +
            "or if an registration token was already generated for the credentials (in which case it will return that token)" +
            "or create a new registration token and return it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A registration token will be returned in the request custom body. The account may have already requested a registration for the current device before. Check response body custom header for more details.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "The credentials are already used by an existing account.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "An exception occurred during the request registration workflow.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/requestRegistration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> requestRegistration(@RequestBody RequestAccount account) {
        var uBoatResponse = authenticationService.requestRegistration(account);

        return switch (uBoatResponse.getHeader()) {
            case ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS -> ResponseEntity.status(HttpStatus.CONFLICT).body(uBoatResponse);
            case ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED,ACCOUNT_ALREADY_PENDING_REGISTRATION
                    -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }

    @Operation(summary = "Register the account specified in the request body by looking for an existing 'RToken' values in the Authorization header and matching account to token from database. " +
            "This token is generated with the /api/requestRegistration API and is set in the Authorization header. " +
            "After the registration is done successfully, a new JWT is generated and sent in the response body. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The registration has been successful and a new JWT is sent in the response body.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "There are problems with the Authorization header or RToken values. Check response custom header for more details.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "406", description = "Token does not match to the account or the request is missing data. Check response custom header for more details.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "An exception occurred during the registration workflow.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> register(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody RequestAccount account) {
        if (!authorizationHeader.contains("RToken "))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatResponse(UBoatStatus.MISSING_RTOKEN, false));

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatResponse(UBoatStatus.INVALID_RTOKEN_FORMAT, false));

        var registrationToken = split[1];

        if (registrationToken.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatResponse(UBoatStatus.INVALID_RTOKEN_FORMAT, false));

        var uBoatResponse = authenticationService.register(account, registrationToken);
        return switch (uBoatResponse.getHeader()) {
            case RTOKEN_NOT_FOUND_IN_DATABASE -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(uBoatResponse);
            case RTOKEN_AND_ACCOUNT_NOT_MATCHING, MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(uBoatResponse);
            case REGISTRATION_SUCCESSFUL -> ResponseEntity.status(HttpStatus.CREATED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
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

    @Operation(summary = "Login into UBoat with the given credentials and generate a new JWT. " +
            "In order to login, the password must match with either the username or phone number. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The credentials match a new JWT is generated in the response body.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "The username/phone number and password don't match.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No account exists with the given credentials.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "An exception occurred during the login workflow.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> login(@RequestBody RequestAccount account) {
        var uBoatResponse = authenticationService.login(account);

        return switch (uBoatResponse.getHeader()) {
            case LOGIN_SUCCESSFUL -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case CREDENTIALS_NOT_FOUND ->ResponseEntity.status(HttpStatus.NOT_FOUND).body(uBoatResponse);
            case INVALID_CREDENTIALS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
        };
    }
}
