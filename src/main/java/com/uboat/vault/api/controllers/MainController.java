package com.uboat.vault.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping(value = "/api/isVaultActive")
    public String isVaultActive() {
        return "Running...";
    }
}
