package com.jerzymaj.energymixgbbackend.unit;

import com.jerzymaj.energymixgbbackend.DTOs.*;
import com.jerzymaj.energymixgbbackend.service.EnergyMixService;
import org.junit.jupiter.api.BeforeEach;
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

    private EnergyMixInterval intervalFirst;
    private EnergyMixInterval intervalSecond;
    private EnergyMixInterval intervalThird;

    @BeforeEach
    public void setUp() {
        intervalFirst = new EnergyMixInterval(
                "2025-12-14T12:00:00Z", "2025-12-14T12:30:00Z",
                List.of(new Fuel("hydro", 30.0), new Fuel("coal", 70.0))
        );

        intervalSecond = new EnergyMixInterval(
                "2025-12-14T12:30:00Z", "2025-12-14T13:00:00Z",
                List.of(new Fuel("hydro", 40.0), new Fuel("coal", 60.0))
        );

        intervalThird = new EnergyMixInterval(
                "2025-12-14T13:00:00Z", "2025-12-14T13:30:00Z",
                List.of(new Fuel("solar", 70.0), new Fuel("coal", 30.0))
        );
    }

    @Test
    public void calculateThreeDaysSummary_ShouldReturnCorrectData() {

        EnergyResponse mockedResponse = new EnergyResponse(List.of(intervalFirst));

        when(restClient.get()
                .uri(anyString(), any(), any())
                .retrieve()
                .body(EnergyResponse.class))
                .thenReturn(mockedResponse);

        List<DailyEnergySummary> actualResult = energyMixService.calculateThreeDaysSummary();

        assertEquals(1, actualResult.size());
        assertEquals(30.0, actualResult.getFirst().cleanEnergyPercent());
    }

    @Test
    public void calculateOptimalChargingWindow() {

        EnergyResponse mockedResponse = new EnergyResponse(List.of(intervalFirst, intervalSecond, intervalThird));

        when(restClient.get()
                .uri(anyString(), any(), any())
                .retrieve()
                .body(EnergyResponse.class))
                .thenReturn(mockedResponse);

        OptimalChargingWindow actualResult = energyMixService.calculateOptimalChargingWindow(1);

        assertEquals(55.0, actualResult.averageCleanEnergyPercent());
        assertEquals("2025-12-14T12:30:00Z", actualResult.startingDateTime());
        assertEquals("2025-12-14T13:30:00Z", actualResult.endingDateTime());
    }
}
