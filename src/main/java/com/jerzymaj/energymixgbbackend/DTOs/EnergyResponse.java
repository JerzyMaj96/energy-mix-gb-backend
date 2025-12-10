package com.jerzymaj.energymixgbbackend.DTOs;

import java.util.List;

public record EnergyResponse(List<EnergyMixInterval> data) {
}
