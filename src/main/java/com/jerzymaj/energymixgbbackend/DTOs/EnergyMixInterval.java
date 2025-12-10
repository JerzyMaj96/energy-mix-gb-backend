package com.jerzymaj.energymixgbbackend.DTOs;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EnergyMixInterval(String from, String to, @JsonProperty("generationmix") List<Fuel> generationMix) {
}
