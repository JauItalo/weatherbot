package com.seunome.weatherbot.dto;

import java.time.LocalDate;

public record DailyForecastItem(
        LocalDate date,
        String dayOfWeek,
        String description,
        String emoji,
        Integer temperatureMax,
        Integer temperatureMin,
        Integer rainProbability
) {}