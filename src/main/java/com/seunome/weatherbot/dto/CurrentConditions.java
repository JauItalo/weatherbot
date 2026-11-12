package com.seunome.weatherbot.dto;

public record CurrentConditions(
        Integer temperature,
        Integer apparentTemperature,
        Integer humidity,
        Integer windSpeed,
        Integer rainProbability,
        String description,
        String emoji,
        String sunrise,
        String sunset
) {}