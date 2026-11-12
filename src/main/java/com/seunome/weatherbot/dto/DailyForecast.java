package com.seunome.weatherbot.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyForecast(
        List<String> time,
        @JsonProperty("weather_code") List<Integer> weatherCode,
        @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
        @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
        @JsonProperty("precipitation_probability_max") List<Integer> precipitationProbabilityMax,
        List<String> sunrise,
        List<String> sunset
) {}