package com.jerzymaj.energymixgbbackend.service;

import com.jerzymaj.energymixgbbackend.DTOs.EnergyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EnergyMixService {

    private final RestClient restClient;

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
}
