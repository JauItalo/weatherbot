package com.seunome.weatherbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentWeather(
        String time,
        @JsonProperty("temperature_2m") Double temperature,
        @JsonProperty("relative_humidity_2m") Integer relativeHumidity,
        @JsonProperty("apparent_temperature") Double apparentTemperature,
        @JsonProperty("is_day") Integer isDay,
        Double precipitation,
        Double rain,
        @JsonProperty("weather_code") Integer weatherCode,
        @JsonProperty("wind_speed_10m") Double windSpeed
) {}