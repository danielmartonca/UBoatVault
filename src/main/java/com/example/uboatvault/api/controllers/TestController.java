package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private final Logger log = LoggerFactory.getLogger(TestController.class);

    @GetMapping(value = "/api/test")
    public String test() {
        log.info(LoggingUtils.logRequestAsString(HttpMethod.GET, "/api/test", null));
        return "Running...";
    }
}
