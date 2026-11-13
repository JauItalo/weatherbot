package com.seunome.weatherbot.dto;

public record ChatResponse(
        ChatResponseType type,
        String message,
        WeatherResponse weather
) {}