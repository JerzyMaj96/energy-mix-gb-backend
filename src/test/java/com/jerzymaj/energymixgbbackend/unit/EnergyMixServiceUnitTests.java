package com.jerzymaj.energymixgbbackend.unit;

import com.jerzymaj.energymixgbbackend.service.EnergyMixService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class EnergyMixServiceUnitTests {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private EnergyMixService energyMixService;

    @Test
    public void calculateDailyEnergySummary() {}

    @Test
    public void calculateThreeDaysSummary() {}
}
