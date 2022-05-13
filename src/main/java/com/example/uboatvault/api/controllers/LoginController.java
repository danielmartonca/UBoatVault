package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.response.LoginResponse;
import com.example.uboatvault.api.model.response.TokenResponse;
import com.example.uboatvault.api.services.*;
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
public class LoginController {
    private final Logger log = LoggerFactory.getLogger(LoginController.class);

    LoginService loginService;
    EncryptionService encryptionService;
    TokenService tokenService;
    CookiesService cookiesService;

    @Autowired
    public LoginController(LoginService loginService, EncryptionService encryptionService, TokenService tokenService, CookiesService cookiesService) {
        this.loginService = loginService;
        this.encryptionService = encryptionService;
        this.tokenService = tokenService;
        this.cookiesService = cookiesService;
    }

    @PostMapping(value = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@CookieValue(name = "token") String token,
                                                  @RequestBody Account account,
                                                  HttpServletResponse response) {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.POST, "/api/login", account));
        log.info("Token is: "+token);

        if (!tokenService.isTokenDecryptable(token)) {
            log.error("Token is not decryptable.");
            return new ResponseEntity<>(new LoginResponse(null, null), HttpStatus.BAD_REQUEST);
        }
        String returnedToken = loginService.login(account, token);
        if (returnedToken == null)
            return new ResponseEntity<>(new LoginResponse(false, null), HttpStatus.OK);
        else {
            cookiesService.addTokenToSetCookiesHeader(token, response);
            return new ResponseEntity<>(new LoginResponse(true, returnedToken), HttpStatus.OK);
        }
    }
}
