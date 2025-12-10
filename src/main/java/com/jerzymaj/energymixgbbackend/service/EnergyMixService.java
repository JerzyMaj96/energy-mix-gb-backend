package com.jerzymaj.energymixgbbackend.service;

import com.jerzymaj.energymixgbbackend.DTOs.DailyEnergySummary;
import com.jerzymaj.energymixgbbackend.DTOs.EnergyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EnergyMixService {

    private final RestClient restClient;

    private static final List<String> RENEWABLES = List.of("biomass", "nuclear", "hydro", "wind", "solar");

    public EnergyMixService(@Value("${api.carbon-intensity.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public EnergyResponse getEnergyData(String from, String to) {

        return restClient.get()
                .uri("/generation/{from}/{to}", from, to)
                .retrieve()
                .body(EnergyResponse.class);
    }

    public DailyEnergySummary calculateDailyEnergySummary(EnergyResponse energyResponse, String date) { //todo continue hear

        return null;
    }

    public List<DailyEnergySummary> calculateThreeDaysSummary() {
        return null;
    }
}
