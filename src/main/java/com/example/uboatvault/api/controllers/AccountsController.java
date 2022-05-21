package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.AccountDetails;
import com.example.uboatvault.api.model.persistence.CreditCard;
import com.example.uboatvault.api.model.requests.CreditCardRequest;
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
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/api/checkUsername/username='" + username + "'"));

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
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestParam String phoneNumber,
                                                    @RequestParam String dialCode,
                                                    @RequestParam String isoCode) {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/api/checkPhoneNumber?phoneNumber='" + phoneNumber + "';" + "dialCode='" + dialCode + "';isoCode='" + isoCode + "'"));

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
    public ResponseEntity<Account> getMissingAccountInformation(@CookieValue(name = "token") String token,
                                                                @RequestBody Account requestAccount) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getMissingAccountInformation", requestAccount));

        Account account = accountsService.getAccountByTokenAndCredentials(token, requestAccount);
        if (account != null) {
            log.info("Account sent back to the user.");

            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getMissingAccountInformation", account));
            return new ResponseEntity<>(account, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");

            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getMissingAccountInformation"));
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/getAccountDetails")
    public ResponseEntity<AccountDetails> getAccountDetails(@CookieValue(name = "token") String token,
                                                            @RequestBody Account requestAccount) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getAccountDetails", requestAccount));

        AccountDetails accountDetails = accountsService.getAccountDetails(token, requestAccount);
        if (accountDetails != null) {
            log.info(LoggingUtils.colorString("Account details sent back to the user.", TextColor.PURPLE));

            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getAccountDetails", accountDetails));
            return new ResponseEntity<>(accountDetails, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getAccountDetails"));
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/updateAccountDetails")
    public ResponseEntity<AccountDetails> updateAccountDetails(@CookieValue(name = "token") String token,
                                                               @RequestBody Account requestAccount) {

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/updateAccountDetails", requestAccount));

        AccountDetails accountDetails = accountsService.updateAccountDetails(token, requestAccount);
        if (accountDetails != null) {
            log.info("Updated account details sent back to the user.");

            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/updateAccountDetails", accountDetails));
            return new ResponseEntity<>(accountDetails, HttpStatus.OK);
        } else {
            log.info("Null sent back to the user.");
            log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/updateAccountDetails"));
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/getCreditCards")
    public ResponseEntity<CreditCardResponse> getCreditCards(@CookieValue(name = "token") String token,
                                                             @RequestBody Account account) {

        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/getCreditCards", account));
        Set<CreditCard> creditCards = accountsService.getCreditCards(token, account);
        if (creditCards == null) {
            log.warn("User is not authorized. Token/account are invalid.");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CreditCardResponse creditCardResponse = new CreditCardResponse(creditCards);
        log.info(LoggingUtils.logResponse(HttpMethod.POST, "/api/getCreditCards", creditCardResponse));
        return new ResponseEntity<>(creditCardResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/addCreditCard")
    public ResponseEntity<Boolean> addCreditCard(@CookieValue(name = "token") String token,
                                                 @RequestBody CreditCardRequest creditCardRequest) {

        log.info(LoggingUtils.logRequest(HttpMethod.PUT, "/api/addCreditCard", creditCardRequest));
        boolean wasAdded = accountsService.addCreditCard(token, creditCardRequest.getAccount(), creditCardRequest.getCard());
        if (wasAdded)
            log.info("Request credit card is linked to the account. Returning true.");
        else
            log.warn("Request credit card was NOT added to the account. Returning false.");

        return new ResponseEntity<>(wasAdded, HttpStatus.CREATED);
    }
}
