package com.example.uboatvault.api.controllers;

import com.example.uboatvault.UBoatVaultApplication;
import com.example.uboatvault.api.configuration.config.WebSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UBoatVaultApplication.class, WebSecurityConfig.class})
public abstract class ControllerTest {
}
