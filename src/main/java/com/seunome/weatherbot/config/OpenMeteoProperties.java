package com.seunome.weatherbot.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weatherbot.open-meteo")
public record OpenMeteoProperties(
        String geocodingUrl,
        String forecastUrl,
        Duration connectTimeout,
        Duration readTimeout
) {}