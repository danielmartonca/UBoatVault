package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.AccountsService;
import com.uboat.vault.api.business.services.AuthenticationService;
import com.uboat.vault.api.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api")
public class AccountsController {
    private final AuthenticationService authenticationService;
    private final AccountsService accountsService;


    @Operation(summary = "Check if the username given as query parameter is already used.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The username provided is not used.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "409", description = "The username provided is already used.", content = @Content(mediaType = "application/json"))})
    @GetMapping(value = "/checkUsername")
    public ResponseEntity<UBoatDTO> checkUsername(@RequestParam String username) {
        var responseBody = authenticationService.checkUsername(username);

        return switch (responseBody.getHeader()) {
            case USERNAME_ACCEPTED, USERNAME_ALREADY_USED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case USERNAME_INVALID_FORMAT -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Check if the phone number composed of actual phone number dial code and iso code given as query parameter is already used.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The phone number provided is not used.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "409", description = "The phone number provided is already used.", content = @Content(mediaType = "application/json"))})
    @GetMapping(value = "/checkPhoneNumber")
    public ResponseEntity<UBoatDTO> checkPhoneNumber(@RequestParam String phoneNumber, @RequestParam String dialCode, @RequestParam String isoCode) {
        var responseBody = authenticationService.checkPhoneNumber(phoneNumber, dialCode, isoCode);

        return switch (responseBody.getHeader()) {
            case PHONE_NUMBER_ACCEPTED, PHONE_NUMBER_ALREADY_USED ->
                    ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case PHONE_NUMBER_INVALID_FORMAT -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Returns the missing username or phone number for the account. If the user has logged in, he can call this API with the JWT and the credentials he used to login.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account username and password retrieved.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header has a malformed format or is missing 'Bearer'.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "401", description = "The credentials in the request don't match with the JWT from the header.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "404", description = "The credentials in the request don't match to any account from the database.", content = @Content(mediaType = "application/json"))})
    @GetMapping(value = "/getMissingAccountInformation")
    public ResponseEntity<UBoatDTO> getMissingAccountInformation(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody AccountDTO accountDTO) {
        var responseBody = accountsService.getMissingAccountInformation(accountDTO, authorizationHeader);

        return switch (responseBody.getHeader()) {
            case MISSING_ACCOUNT_INFORMATION_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case ACCOUNT_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID, CREDENTIALS_NOT_MATCHING_JWT ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Returns the account details such as email and full name. Credentials and very sensitive data will not be displayed.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account details of the account extracted from JWT  will be returned.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header is missing 'Bearer',has a malformed format or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/getAccountDetails")
    public ResponseEntity<UBoatDTO> getAccountDetails(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var responseBody = accountsService.getAccountDetails(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case ACCOUNT_DETAILS_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Updates details such as email and full name. Fields that are not empty/null will update their corresponding data in the database.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The response body will contain custom body 'true' meaning the data was updated successfully.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header is missing 'Bearer',has a malformed format or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @PatchMapping(value = "/updateAccountDetails")
    public ResponseEntity<UBoatDTO> updateAccountDetails(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody AccountDetailsDTO accountDetails) {
        var responseBody = accountsService.updateAccountDetails(authorizationHeader, accountDetails);

        return switch (responseBody.getHeader()) {
            case ACCOUNT_DETAILS_UPDATED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Gets the credit cards details (without CVC) of the user in the JWT.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The response body will contain custom body of credit cards information.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header is missing 'Bearer',has a malformed format or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/getCreditCards")
    public ResponseEntity<UBoatDTO> getCreditCards(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var responseBody = accountsService.getCreditCards(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case CREDIT_CARDS_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Adds a new credit card to the account from the JWT if the card is valid and it is not already existing by number and full owner name to the given account.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "The credit card is bound to the account.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "406", description = "The credit card is not valid. For more details check the output of the response body custom header.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header is missing 'Bearer',has a malformed format or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @PostMapping(value = "/addCreditCard")
    public ResponseEntity<UBoatDTO> addCreditCard(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody CreditCardDTO creditCard) {
        var responseBody = accountsService.addCreditCard(authorizationHeader, creditCard);

        return switch (responseBody.getHeader()) {
            case CREDIT_CARD_ADDED, CREDIT_CARD_DUPLICATE ->
                    ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
            case CREDIT_CARD_EXPIRED -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Deletes credit card from the account of the JWT by Owner Name and Number if the card is bounded to the account.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The credit card was deleted from the account.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "404", description = "The account does not have the given credit card.", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The authorization header is missing 'Bearer',has a malformed format or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @DeleteMapping(value = "/deleteCreditCard")
    public ResponseEntity<UBoatDTO> deleteCreditCard(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody CreditCardDTO creditCardRequest) {
        var responseBody = accountsService.deleteCreditCard(authorizationHeader, creditCardRequest);

        return switch (responseBody.getHeader()) {
            case CREDIT_CARD_DELETED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case CREDIT_CARD_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Gets the boat details for the sailor account extracted from the JWT.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The boat was retrieved successfully", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The JWT does is not corresponding to a sailor account, " + "the authorization header is missing 'Bearer', " + "has a malformed format " + "or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/getMyBoat")
    public ResponseEntity<UBoatDTO> getMyBoat(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var responseBody = accountsService.getMyBoat(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case BOAT_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Updates boat details for the sailor account extracted from the JWT.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The boat was updated successfully", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The JWT does is not corresponding to a sailor account, " + "the authorization header is missing 'Bearer', " + "has a malformed format " + "or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @PatchMapping(value = "/updateMyBoat")
    public ResponseEntity<UBoatDTO> updateMyBoat(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody BoatDTO boat) {
        var responseBody = accountsService.updateMyBoat(authorizationHeader, boat);

        return switch (responseBody.getHeader()) {
            case BOAT_UPDATED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Retrieves information regarding the sailor.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The boat was updated successfully", content = @Content(mediaType = "application/json")), @ApiResponse(responseCode = "400", description = "The JWT does is not corresponding to a sailor account, " + "the authorization header is missing 'Bearer', " + "has a malformed format " + "or the JWT has expired/has problems.", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/getSailorDetails")
    public ResponseEntity<UBoatDTO> getSailorDetails(@RequestParam String sailorId) {
        var responseBody = accountsService.getSailorDetails(sailorId);

        return switch (responseBody.getHeader()) {
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            case SAILOR_DETAILS_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case MISSING_BEARER, INVALID_BEARER_FORMAT, JWT_INVALID ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }
}
