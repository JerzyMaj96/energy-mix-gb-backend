package com.jerzymaj.energymixgbbackend.DTOs;

import java.util.Map;

public record DailyEnergySummary(String date, double cleanEnergyPercent, Map<String, Double> fuelSpec) {
}
