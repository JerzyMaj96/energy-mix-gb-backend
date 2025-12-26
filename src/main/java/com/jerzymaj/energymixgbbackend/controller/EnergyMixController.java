package com.jerzymaj.energymixgbbackend.controller;

import com.jerzymaj.energymixgbbackend.DTOs.DailyEnergySummary;
import com.jerzymaj.energymixgbbackend.DTOs.OptimalChargingWindow;
import com.jerzymaj.energymixgbbackend.service.EnergyMixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/energy-mix")
@RequiredArgsConstructor
public class EnergyMixController {

    private final EnergyMixService energyMixService;

    @GetMapping("/three-days-summary")
    public ResponseEntity<List<DailyEnergySummary>> retrieveThreeDaysSummary() {
        return ResponseEntity.ok(energyMixService.calculateThreeDaysSummary());
    }

    @GetMapping("/optimal-charging-window")
    public ResponseEntity<OptimalChargingWindow> retrieveOptimalChargingWindow(@RequestParam int windowLength) {

        return ResponseEntity.ok(energyMixService.calculateOptimalChargingWindow(windowLength));
    }
}
