package com.jerzymaj.energymixgbbackend.integration;

import com.jerzymaj.energymixgbbackend.DTOs.DailyEnergySummary;
import com.jerzymaj.energymixgbbackend.DTOs.OptimalChargingWindow;
import com.jerzymaj.energymixgbbackend.controller.EnergyMixController;
import com.jerzymaj.energymixgbbackend.service.EnergyMixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnergyMixController.class)
public class EnergyMixControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyMixService energyMixService;

    @Test
    public void calculateThreeDaysSummary() throws Exception {

        DailyEnergySummary summary = new DailyEnergySummary(
                "2025-12-14", 50.0, Map.of("wind", 50.0, "coal", 50.0));

        List<DailyEnergySummary> mockSummaryList = List.of(summary);

        when(energyMixService.calculateThreeDaysSummary()).thenReturn(mockSummaryList);

        mockMvc.perform(get("/energy-mix/three-days-summary")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].date").value("2025-12-14"))
                .andExpect(jsonPath("$[0].cleanEnergyPercent").value(50.0));
    }

    @Test
    public void calculateOptimalChargingWindow() throws Exception {

        OptimalChargingWindow optimalChargingWindow = new OptimalChargingWindow(
                "2025-12-14T12:00:00Z",
                "2025-12-14T14:00:00Z",
                85.5
        );

        when(energyMixService.calculateOptimalChargingWindow(anyInt())).thenReturn(optimalChargingWindow);

        mockMvc.perform(get("/energy-mix/optimal-charging-window")
                        .param("windowLength", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startingDateTime").value("2025-12-14T12:00:00Z"))
                .andExpect(jsonPath("$.endingDateTime").value("2025-12-14T14:00:00Z"))
                .andExpect(jsonPath("$.averageCleanEnergyPercent").value(85.5));


    }

}
