package com.seunome.weatherbot.controller;

import com.seunome.weatherbot.dto.ErrorResponse;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Clima", description = "Consulta estruturada de clima e previsão")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(
            summary = "Consulta o clima de uma cidade",
            description = "Retorna o clima atual e a previsão de 7 dias. "
                    + "Usa o primeiro resultado do geocoding; para cidades homônimas, "
                    + "prefira o endpoint /api/chat.")
    @ApiResponse(responseCode = "200", description = "Clima encontrado")
    @ApiResponse(responseCode = "400", description = "Parâmetro ausente ou inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Cidade não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Open-Meteo indisponível",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/weather")
    public WeatherResponse getWeather(
            @Parameter(description = "Nome da cidade", example = "Lisboa")
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 100)
            String city) {
        return weatherService.getWeatherByCity(city.trim());
    }
}