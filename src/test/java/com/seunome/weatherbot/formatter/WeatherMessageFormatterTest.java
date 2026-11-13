package com.seunome.weatherbot.formatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.seunome.weatherbot.dto.CurrentConditions;
import com.seunome.weatherbot.dto.DailyForecastItem;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.LocationInfo;
import com.seunome.weatherbot.dto.WeatherResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeatherMessageFormatterTest {

    private final WeatherMessageFormatter formatter = new WeatherMessageFormatter();

    @Test
    void deveFormatarPrevisaoCompleta() {
        String texto = formatter.formatWeather(weatherExemplo());

        assertTrue(texto.contains("Previsão do tempo — Lisboa, Lisboa, Portugal"));
        assertTrue(texto.contains("Temperatura: 22°C"));
        assertTrue(texto.contains("Sensação térmica: 24°C"));
        assertTrue(texto.contains("Umidade: 65%"));
        assertTrue(texto.contains("Vento: 12 km/h"));
        assertTrue(texto.contains("Chuva: 10%"));
        assertTrue(texto.contains("Condição: Parcialmente nublado"));
        assertTrue(texto.contains("Nascer do sol: 06:52"));
        assertTrue(texto.contains("Pôr do sol: 17:21"));
        assertTrue(texto.contains("Sexta-feira"));
        assertTrue(texto.contains("25°C / 17°C"));
    }

    @Test
    void deveUsarTracosQuandoDadosAusentes() {
        CurrentConditions vazio = new CurrentConditions(
                null, null, null, null, null, "Nublado", "☁️", null, null);
        WeatherResponse weather = new WeatherResponse(
                new LocationInfo("Lisboa", null, "Portugal", "Europe/Lisbon", 0.0, 0.0),
                vazio,
                List.of());

        String texto = formatter.formatWeather(weather);

        assertTrue(texto.contains("Temperatura: --"));
        assertTrue(texto.contains("Nascer do sol: --"));
        assertFalse(texto.contains("null"));
    }

    @Test
    void deveListarOpcoesNaDesambiguacao() {
        List<GeocodingResult> opcoes = List.of(
                new GeocodingResult(1L, "Santa Cruz", 0.0, 0.0, "BR", "Brasil",
                        "Rio Grande do Norte", "America/Fortaleza", 1000L),
                new GeocodingResult(2L, "Santa Cruz", 0.0, 0.0, "BO", "Bolívia",
                        "Santa Cruz", "America/La_Paz", 900L));

        String texto = formatter.formatDisambiguation("Santa Cruz", opcoes);

        assertTrue(texto.contains("1. Santa Cruz, Rio Grande do Norte, Brasil"));
        assertTrue(texto.contains("2. Santa Cruz, Santa Cruz, Bolívia"));
        assertTrue(texto.contains("Santa Cruz, Rio Grande do Norte"));
    }

    @Test
    void deveFormatarCidadeNaoEncontrada() {
        String texto = formatter.formatNotFound("xyz");
        assertTrue(texto.contains("\"xyz\""));
    }

    private WeatherResponse weatherExemplo() {
        return new WeatherResponse(
                new LocationInfo("Lisboa", "Lisboa", "Portugal", "Europe/Lisbon", 38.7, -9.1),
                new CurrentConditions(22, 24, 65, 12, 10,
                        "Parcialmente nublado", "⛅", "06:52", "17:21"),
                List.of(new DailyForecastItem(
                        LocalDate.of(2026, 11, 13), "Sexta-feira",
                        "Céu limpo", "☀️", 25, 17, 10)));
    }
}