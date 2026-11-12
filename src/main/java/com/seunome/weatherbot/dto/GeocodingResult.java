package com.seunome.weatherbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResult(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        @JsonProperty("country_code") String countryCode,
        String country,
        String admin1,
        String timezone,
        Long population
) {}