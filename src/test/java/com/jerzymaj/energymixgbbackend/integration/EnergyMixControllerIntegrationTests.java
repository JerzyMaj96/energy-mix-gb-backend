package com.jerzymaj.energymixgbbackend.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnergyMixControllerIntegrationTests {

    private MockMvc mockMvc;
}
