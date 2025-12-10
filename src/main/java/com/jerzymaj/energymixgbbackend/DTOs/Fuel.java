package com.jerzymaj.energymixgbbackend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Fuel(String fuel, @JsonProperty("perc") double percentage) {
}
