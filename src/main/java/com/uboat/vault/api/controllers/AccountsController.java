package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.requests.CreditCardRequest;
import com.uboat.vault.api.model.http.requests.UpdateBoatRequest;
import com.uboat.vault.api.model.http.response.CreditCardResponse;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.services.AccountsService;
import com.uboat.vault.api.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class AccountsController {
    private final Logger log = LoggerFactory.getLogger(AccountsController.class);

    private final AuthenticationService authenticationService;
    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AuthenticationService authenticationService, AccountsService accountsService) {
        this.authenticationService = authenticationService;
        this.accountsService = accountsService;
    }

    @Operation(summary = "Check if the username given as query parameter is already used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The username provided is not used.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "The username provided is already used.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/checkUsername")
    public ResponseEntity<UBoatResponse> checkUsername(@RequestParam String username) {
        var isUsed = authenticationService.checkUsername(username);
        if (isUsed)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UBoatResponse(UBoatStatus.USERNAME_ALREADY_USED, false));
        return ResponseEntity.status(HttpStatus.OK).body(new UBoatResponse(UBoatStatus.USERNAME_ACCEPTED, true));
    }

    @Operation(summary = "Check if the phone number composed of actual phone number dial code and iso code given as query parameter is already used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The phone number provided is not used.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "The phone number provided is already used.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/checkPhoneNumber")
    public ResponseEntity<UBoatResponse> checkPhoneNumber(@RequestParam String phoneNumber, @RequestParam String dialCode, @RequestParam String isoCode) {
        var isUsed = authenticationService.checkPhoneNumber(phoneNumber, dialCode, isoCode);

        if (isUsed)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UBoatResponse(UBoatStatus.PHONE_NUMBER_ALREADY_USED, false));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new UBoatResponse(UBoatStatus.PHONE_NUMBER_ACCEPTED, true));
    }

    @PostMapping(value = "/getMissingAccountInformation")
    public ResponseEntity<Account> getMissingAccountInformation(@RequestBody Account requestAccount) {
        Account account = accountsService.getMissingAccountInformation(requestAccount);
        return ResponseEntity.ok(account);
    }

    @PostMapping(value = "/getAccountDetails")
    public ResponseEntity<AccountDetails> getAccountDetails(@RequestBody Account requestAccount) {
        var accountDetails = accountsService.getAccountDetails(requestAccount);
        return ResponseEntity.ok(accountDetails);
    }

    @PostMapping(value = "/updateAccountDetails")
    public ResponseEntity<AccountDetails> updateAccountDetails(@RequestBody Account requestAccount) {
        var accountDetails = accountsService.updateAccountDetails(requestAccount);
        return ResponseEntity.ok(accountDetails);
    }

    @PostMapping(value = "/getCreditCards")
    public ResponseEntity<CreditCardResponse> getCreditCards(@RequestBody Account account) {
        var creditCards = accountsService.getCreditCards(account);
        if (creditCards == null) {
            log.warn("User is not authorized. Account is invalid.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(new CreditCardResponse(creditCards));
    }

    @PutMapping(value = "/addCreditCard")
    public ResponseEntity<Boolean> addCreditCard(@RequestBody CreditCardRequest creditCardRequest) {
        Boolean wasAdded = accountsService.addCreditCard(creditCardRequest.getAccount(), creditCardRequest.getCard());

        if (wasAdded == null) {
            log.warn("Token and account are not matching. Returning null.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        if (wasAdded) {
            log.info("Request credit card is linked to the account. Returning true.");
            return new ResponseEntity<>(true, HttpStatus.CREATED);
        } else {
            log.warn("Request credit card was NOT added to the account. Returning false.");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

    }

    @DeleteMapping(value = "/deleteCreditCard")
    public ResponseEntity<Boolean> deleteCreditCard(@RequestBody CreditCardRequest creditCardRequest) {

        Boolean wasDeleted = accountsService.deleteCreditCard(creditCardRequest.getAccount(), creditCardRequest.getCard());

        if (wasDeleted == null) {
            log.warn("Token and account are not matching. Returning null.");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        if (wasDeleted) {
            log.info("Request credit card was deleted from the account. Returning true.");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            log.warn("Request credit card was NOT deleted from the account. Returning false.");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

    }

    @GetMapping(value = "/getSailorName")
    public ResponseEntity<String> getSailorName(@RequestParam String sailorId) {
        var sailorName = accountsService.getSailorName(sailorId);
        return ResponseEntity.ok(sailorName);
    }

    @PostMapping(value = "/getBoat")
    public ResponseEntity<Boat> getBoat(@RequestBody Account requestAccount) {
        var boat = accountsService.getBoat(requestAccount);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/updateBoat")
    public ResponseEntity<Boat> updateBoat(@RequestBody UpdateBoatRequest request) {
        var boat = accountsService.updateBoat(request.getAccount(), request.getBoat());

        if (boat != null) {
            log.info("Updated boat and sending it back to the user without the images.");
            return ResponseEntity.ok(request.getBoat());
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
