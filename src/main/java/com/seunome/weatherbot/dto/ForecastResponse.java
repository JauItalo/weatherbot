package com.seunome.weatherbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastResponse(
        Double latitude,
        Double longitude,
        String timezone,
        @JsonProperty("utc_offset_seconds") Integer utcOffsetSeconds,
        CurrentWeather current,
        DailyForecast daily
) {}