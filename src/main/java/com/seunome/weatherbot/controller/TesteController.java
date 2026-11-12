package com.seunome.weatherbot.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seunome.weatherbot.client.ForecastClient;
import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ForecastResponse;
import com.seunome.weatherbot.dto.GeocodingResult;

@RestController
public class TesteController {

    private final GeocodingClient geocodingClient;
    private final ForecastClient forecastClient;

    public TesteController(GeocodingClient geocodingClient, ForecastClient forecastClient) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
    }

    @GetMapping("/teste/cidades")
    public List<GeocodingResult> cidades(@RequestParam String nome) {
        return geocodingClient.searchCity(nome);
    }

    @GetMapping("/teste/clima")
    public ForecastResponse clima(@RequestParam double lat, @RequestParam double lon) {
        return forecastClient.getForecast(lat, lon);
    }
}