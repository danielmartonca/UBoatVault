package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.model.requests.CreditCardRequest;
import com.uboat.vault.api.model.requests.UpdateBoatRequest;
import com.uboat.vault.api.model.response.CreditCardResponse;
import com.uboat.vault.api.services.AccountsService;
import com.uboat.vault.api.services.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountsController {
    private final Logger log = LoggerFactory.getLogger(AccountsController.class);

    private final AuthenticationService authenticationService;
    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AuthenticationService authenticationService, AccountsService accountsService) {
        this.authenticationService = authenticationService;
        this.accountsService = accountsService;
    }

    @GetMapping(value = "/api/checkUsername")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        var isUsed = authenticationService.checkUsername(username);
        return ResponseEntity.ok(isUsed);
    }

    @GetMapping(value = "/api/checkPhoneNumber")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber, @RequestParam String dialCode, @RequestParam String isoCode) {
        var isUsed = authenticationService.checkPhoneNumber(phoneNumber, dialCode, isoCode);
        return ResponseEntity.ok(isUsed);
    }

    @PostMapping(value = "/api/getMissingAccountInformation")
    public ResponseEntity<Account> getMissingAccountInformation(@RequestBody Account requestAccount) {
        Account account = accountsService.getMissingAccountInformation(requestAccount);
        return ResponseEntity.ok(account);
    }

    @PostMapping(value = "/api/getAccountDetails")
    public ResponseEntity<AccountDetails> getAccountDetails(@RequestBody Account requestAccount) {
        var accountDetails = accountsService.getAccountDetails(requestAccount);
        return ResponseEntity.ok(accountDetails);
    }

    @PostMapping(value = "/api/updateAccountDetails")
    public ResponseEntity<AccountDetails> updateAccountDetails(@RequestBody Account requestAccount) {
        var accountDetails = accountsService.updateAccountDetails(requestAccount);
        return ResponseEntity.ok(accountDetails);
    }

    @PostMapping(value = "/api/getCreditCards")
    public ResponseEntity<CreditCardResponse> getCreditCards(@RequestBody Account account) {
        var creditCards = accountsService.getCreditCards(account);
        if (creditCards == null) {
            log.warn("User is not authorized. Account is invalid.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(new CreditCardResponse(creditCards));
    }

    @PutMapping(value = "/api/addCreditCard")
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

    @DeleteMapping(value = "/api/deleteCreditCard")
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

    @GetMapping(value = "/api/getSailorName")
    public ResponseEntity<String> getSailorName(@RequestParam String sailorId) {
        var sailorName = accountsService.getSailorName(sailorId);
        return ResponseEntity.ok(sailorName);
    }

    @PostMapping(value = "/api/getBoat")
    public ResponseEntity<Boat> getBoat(@RequestBody Account requestAccount) {
        var boat = accountsService.getBoat(requestAccount);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/api/updateBoat")
    public ResponseEntity<Boat> updateBoat(@RequestBody UpdateBoatRequest request) {
        var boat = accountsService.updateBoat(request.getAccount(), request.getBoat());

        if (boat != null) {
            log.info("Updated boat and sending it back to the user without the images.");
            return ResponseEntity.ok(request.getBoat());
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
