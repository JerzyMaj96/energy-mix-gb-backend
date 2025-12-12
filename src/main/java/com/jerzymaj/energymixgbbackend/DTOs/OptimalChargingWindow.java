package com.jerzymaj.energymixgbbackend.DTOs;

public record OptimalChargingWindow(String startingDateTime, String endingDateTime, double averageCleanEnergyPercent) {
}
