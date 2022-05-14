package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.response.LoginResponse;
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

    private final LoginService loginService;
    private final TokenService tokenService;
    private final CookiesService cookiesService;

    @Autowired
    public LoginController(LoginService loginService, TokenService tokenService, CookiesService cookiesService) {
        this.loginService = loginService;
        this.tokenService = tokenService;
        this.cookiesService = cookiesService;
    }

    @PostMapping(value = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@CookieValue(name = "token") String token,
                                               @RequestBody Account account,
                                               HttpServletResponse response) {
        log.info(LoggingUtils.logRequest(HttpMethod.POST, "/api/login", account));

        if (tokenService.isTokenInvalid(token)) {
            log.error("Token is not decryptable.");
            return new ResponseEntity<>(new LoginResponse(null, null), HttpStatus.BAD_REQUEST);
        }

        String returnedToken = loginService.login(account, token);
        if (returnedToken == null) {
            log.info("Login denied.");
            return new ResponseEntity<>(new LoginResponse(false, null), HttpStatus.OK);
        } else {
            {
                cookiesService.addTokenToSetCookiesHeader(token, response);
                log.info("Login request accepted. Sending back new token.");
                return new ResponseEntity<>(new LoginResponse(true, returnedToken), HttpStatus.OK);
            }
        }
    }
}
