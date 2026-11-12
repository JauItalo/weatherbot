package com.seunome.weatherbot.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seunome.weatherbot.client.ForecastClient;
import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ForecastResponse;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.service.WeatherService;

@RestController
public class TesteController {

    private final GeocodingClient geocodingClient;
    private final ForecastClient forecastClient;
    private final WeatherService weatherService;

    public TesteController(GeocodingClient geocodingClient,
                           ForecastClient forecastClient,
                           WeatherService weatherService) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
        this.weatherService = weatherService;
    }

    @GetMapping("/teste/cidades")
    public List<GeocodingResult> cidades(@RequestParam String nome) {
        return geocodingClient.searchCity(nome);
    }

    @GetMapping("/teste/clima")
    public ForecastResponse clima(@RequestParam double lat, @RequestParam double lon) {
        return forecastClient.getForecast(lat, lon);
    }

    @GetMapping("/teste/weather")
    public WeatherResponse weather(@RequestParam String city) {
        return weatherService.getWeatherByCity(city);
    }
}