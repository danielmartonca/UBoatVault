package com.uboat.vault.api.controllers;

import com.uboat.vault.UBoatVaultApplication;
import com.uboat.vault.api.configuration.config.WebSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UBoatVaultApplication.class, WebSecurityConfig.class})
public abstract class ControllerTest {
}
