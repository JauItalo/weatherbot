package com.seunome.weatherbot.dto;

import java.util.List;

public record ChatResponse(
        ChatResponseType type,
        String message,
        WeatherResponse weather,
        List<ChatOption> options
) {}
