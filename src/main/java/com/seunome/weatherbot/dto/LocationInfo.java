package com.seunome.weatherbot.dto;

public record LocationInfo(
        String city,
        String state,
        String country,
        String timezone,
        Double latitude,
        Double longitude
) {}