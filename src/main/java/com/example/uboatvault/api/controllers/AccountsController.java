package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.info.AccountDetails;
import com.example.uboatvault.api.model.persistence.account.info.CreditCard;
import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import com.example.uboatvault.api.model.requests.CreditCardRequest;
import com.example.uboatvault.api.model.requests.UpdateBoatRequest;
import com.example.uboatvault.api.model.response.CreditCardResponse;
import com.example.uboatvault.api.services.AccountsService;
import com.example.uboatvault.api.services.RegistrationService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import com.example.uboatvault.api.utility.logging.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class AccountsController {
    private final Logger log = LoggerFactory.getLogger(AccountsController.class);

    private final RegistrationService registrationService;
    private final AccountsService accountsService;

    @Autowired
    public AccountsController(RegistrationService registrationService, AccountsService accountsService) {
        this.registrationService = registrationService;
        this.accountsService = accountsService;
    }


    @GetMapping(value = "/api/checkUsername")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {

        if (!registrationService.usernameMatchesPattern(username)) {
            log.info("Username doesn't match pattern.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!registrationService.isUsernameUsed(username)) {
            log.info("Username is not used.");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            log.info("Username is already used.");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/api/checkPhoneNumber")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber, @RequestParam String dialCode, @RequestParam String isoCode) {

        if (!registrationService.phoneNumberMatchesPattern(phoneNumber) || dialCode.length() > 5 || isoCode.length() >= 3) {
            log.info("Phone number doesn't match pattern or dial code/iso code too long .");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!registrationService.isPhoneNumberUsed(phoneNumber, dialCode, isoCode)) {
            log.info("Phone number is already used.");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            log.info("Phone number is not used.");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/getMissingAccountInformation")
    public ResponseEntity<Account> getMissingAccountInformation(@CookieValue(name = "token") String token, @RequestBody Account requestAccount) {

        Account account = accountsService.getAccountByTokenAndCredentials(token, requestAccount);
        if (account != null) {
            log.info("Account sent back to the user.");

            return new ResponseEntity<>(account, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");

            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/getAccountDetails")
    public ResponseEntity<AccountDetails> getAccountDetails(@CookieValue(name = "token") String token, @RequestBody Account requestAccount) {

        AccountDetails accountDetails = accountsService.getAccountDetails(token, requestAccount);
        if (accountDetails != null) {

            return new ResponseEntity<>(accountDetails, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/updateAccountDetails")
    public ResponseEntity<AccountDetails> updateAccountDetails(@CookieValue(name = "token") String token, @RequestBody Account requestAccount) {


        AccountDetails accountDetails = accountsService.updateAccountDetails(token, requestAccount);
        if (accountDetails != null) {
            log.info("Updated account details sent back to the user.");

            return new ResponseEntity<>(accountDetails, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/getCreditCards")
    public ResponseEntity<CreditCardResponse> getCreditCards(@CookieValue(name = "token") String token, @RequestBody Account account) {

        Set<CreditCard> creditCards = accountsService.getCreditCards(token, account);
        if (creditCards == null) {
            log.warn("User is not authorized. Token/account are invalid.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CreditCardResponse creditCardResponse = new CreditCardResponse(creditCards);
        return new ResponseEntity<>(creditCardResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/addCreditCard")
    public ResponseEntity<Boolean> addCreditCard(@CookieValue(name = "token") String token, @RequestBody CreditCardRequest creditCardRequest) {

        Boolean wasAdded = accountsService.addCreditCard(token, creditCardRequest.getAccount(), creditCardRequest.getCard());

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
    public ResponseEntity<Boolean> deleteCreditCard(@CookieValue(name = "token") String token, @RequestBody CreditCardRequest creditCardRequest) {

        Boolean wasDeleted = accountsService.deleteCreditCard(token, creditCardRequest.getAccount(), creditCardRequest.getCard());

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
    public ResponseEntity<String> getSailorName(@CookieValue(name = "token") String token, @RequestParam String sailorId) {

        var sailorName = accountsService.getSailorName(token, sailorId);
        if (sailorName == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);


        return new ResponseEntity<>(sailorName, HttpStatus.OK);
    }

    @PostMapping(value = "/api/getBoat")
    public ResponseEntity<Boat> getBoat(@CookieValue(name = "token") String token, @RequestBody Account requestAccount) {
        var boat = accountsService.getBoat(token, requestAccount);
        return new ResponseEntity<>(boat, HttpStatus.OK);
    }

    @PostMapping(value = "/api/updateBoat")
    public ResponseEntity<Boat> updateBoat(@CookieValue(name = "token") String token, @RequestBody UpdateBoatRequest request) {
        var boat = accountsService.updateBoat(token, request.getAccount(), request.getBoat());
        if (boat != null) {
            log.info("Updated boat and sending it back to the user without the images.");
            return new ResponseEntity<>(request.getBoat(), HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }
}
