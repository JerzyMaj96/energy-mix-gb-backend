package com.jerzymaj.energymixgbbackend.unit;

import com.jerzymaj.energymixgbbackend.DTOs.DailyEnergySummary;
import com.jerzymaj.energymixgbbackend.DTOs.EnergyMixInterval;
import com.jerzymaj.energymixgbbackend.DTOs.EnergyResponse;
import com.jerzymaj.energymixgbbackend.DTOs.Fuel;
import com.jerzymaj.energymixgbbackend.service.EnergyMixService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnergyMixServiceUnitTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks
    private EnergyMixService energyMixService;

    @Test
    public void calculateThreeDaysSummary_ShouldReturnCorrectData() {
        EnergyMixInterval interval = new EnergyMixInterval(
                "2025-12-14T12:00:00Z", "2025-12-14T12:30:00Z",
                List.of(new Fuel("wind", 30.0), new Fuel("coal", 70.0))
        );

        EnergyResponse mockedResponse = new EnergyResponse(List.of(interval));

        when(restClient.get()
                .uri(anyString(), any(), any())
                .retrieve()
                .body(EnergyResponse.class))
                .thenReturn(mockedResponse);

        List<DailyEnergySummary> actualResult = energyMixService.calculateThreeDaysSummary();

        assertEquals(1, actualResult.size());
        assertEquals(30.0, actualResult.getFirst().cleanEnergyPercent());
    }
}
