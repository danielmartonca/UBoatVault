package com.uboat.vault.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(MainController.class);

    @GetMapping(value = "/api/isVaultActive")
    public String isVaultActive() {
        return "Running...";
    }
}
