package com.seunome.weatherbot.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.service.WeatherService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public WeatherResponse getWeather(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String city) {
        return weatherService.getWeatherByCity(city.trim());
    }
}