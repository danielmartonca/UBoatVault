package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(MainController.class);

    @GetMapping(value = "/api/isVaultActive")
    public String test() {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/api/isVaultActive", null));
        log.info(LoggingUtils.logResponse(HttpMethod.GET, "/api/isVaultActive", "Running..."));
        return "Running...";
    }
}
