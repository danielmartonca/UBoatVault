package com.uboat.vault.api.controllers;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("junit")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public abstract class ControllerTest {
}
