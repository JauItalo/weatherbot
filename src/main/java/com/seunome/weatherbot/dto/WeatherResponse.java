package com.seunome.weatherbot.dto;

import java.util.List;

public record WeatherResponse(
        LocationInfo location,
        CurrentConditions current,
        List<DailyForecastItem> daily
) {}