package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.RegistrationData;
import com.example.uboatvault.api.model.response.RegistrationDataResponse;
import com.example.uboatvault.api.model.response.TokenResponse;
import com.example.uboatvault.api.services.CookiesService;
import com.example.uboatvault.api.services.EncryptionService;
import com.example.uboatvault.api.services.RegistrationService;
import com.example.uboatvault.api.services.TokenService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class TokenController {
    private final Logger log = LoggerFactory.getLogger(TokenController.class);

    RegistrationService registrationService;
    EncryptionService encryptionService;
    TokenService tokenService;
    CookiesService cookiesService;

    @Autowired
    public TokenController(RegistrationService registrationService, EncryptionService encryptionService, TokenService tokenService, CookiesService cookiesService) {
        this.registrationService = registrationService;
        this.encryptionService = encryptionService;
        this.tokenService = tokenService;
        this.cookiesService = cookiesService;
    }

    @PostMapping(value = "/api/requestToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<TokenResponse> requestToken(@RequestBody Account account, HttpServletResponse response) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/requestToken", account));
        String token = tokenService.requestToken(account);
        TokenResponse tokenResponse;
        if (token != null) {
            cookiesService.addTokenToSetCookiesHeader(token, response);
            log.info("Returning token.");
            tokenResponse = new TokenResponse(true, token);
        } else {
            log.warn("The requested account does not exist or is missing information.");
            tokenResponse = new TokenResponse(false, null);
        }

        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }
}
