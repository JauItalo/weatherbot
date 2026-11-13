package com.seunome.weatherbot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.seunome.weatherbot.client.ForecastClient;
import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.exception.WeatherApiUnavailableException;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;
import com.seunome.weatherbot.service.ChatService;
import com.seunome.weatherbot.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

class WeatherBotIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.baseUrl())
                .build();

        GeocodingClient geocodingClient = new GeocodingClient(restClient);
        ForecastClient forecastClient = new ForecastClient(restClient);
        WeatherService weatherService = new WeatherService(geocodingClient, forecastClient);

        chatService = new ChatService(geocodingClient, weatherService, new WeatherMessageFormatter());
    }

    @Test
    void deveRetornarPrevisaoCompletaParaCidadeUnica() {
        stubGeocoding("Lisboa", """
                {"results":[{"id":1,"name":"Lisboa","latitude":38.71,"longitude":-9.13,
                 "country_code":"PT","country":"Portugal","admin1":"Lisboa",
                 "timezone":"Europe/Lisbon","population":500000}]}""");
        stubForecast();

        ChatResponse r = chatService.handle("Lisboa");

        assertEquals(ChatResponseType.WEATHER, r.type());
        assertEquals("Lisboa", r.weather().location().city());
        assertEquals(22, r.weather().current().temperature());
        assertEquals("Parcialmente nublado", r.weather().current().description());
        assertEquals("06:52", r.weather().current().sunrise());
        assertEquals(2, r.weather().daily().size());
        assertEquals("Sexta-feira", r.weather().daily().get(0).dayOfWeek());
        assertEquals("Sábado", r.weather().daily().get(1).dayOfWeek());
        assertTrue(r.message().contains("Temperatura: 22°C"));
    }

    @Test
    void deveGerarDesambiguacaoParaCidadesHomonimas() {
        stubGeocoding("Santa Cruz", """
                {"results":[
                 {"id":1,"name":"Santa Cruz","latitude":-6.2,"longitude":-36.0,
                  "country_code":"BR","country":"Brasil","admin1":"Rio Grande do Norte",
                  "timezone":"America/Fortaleza","population":40000},
                 {"id":2,"name":"Santa Cruz","latitude":-17.7,"longitude":-63.1,
                  "country_code":"BO","country":"Bolívia","admin1":"Santa Cruz",
                  "timezone":"America/La_Paz","population":35000}]}""");

        ChatResponse r = chatService.handle("Santa Cruz");

        assertEquals(ChatResponseType.DISAMBIGUATION, r.type());
        assertTrue(r.message().contains("Brasil"));
        assertTrue(r.message().contains("Bolívia"));
    }

    @Test
    void deveRetornarNotFoundQuandoApiNaoTemResultados() {
        stubGeocoding("xyzabc123", "{\"generationtime_ms\":0.5}");

        ChatResponse r = chatService.handle("xyzabc123");

        assertEquals(ChatResponseType.NOT_FOUND, r.type());
    }

    @Test
    void deveLancarExcecaoQuandoForecastEstaIndisponivel() {
        stubGeocoding("Lisboa", """
                {"results":[{"id":1,"name":"Lisboa","latitude":38.71,"longitude":-9.13,
                 "country_code":"PT","country":"Portugal","admin1":"Lisboa",
                 "timezone":"Europe/Lisbon","population":500000}]}""");
        wireMock.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(WeatherApiUnavailableException.class, () -> chatService.handle("Lisboa"));
    }

    // ---------- stubs ----------

    private void stubGeocoding(String name, String json) {
        wireMock.stubFor(get(urlPathEqualTo("/v1/search"))
                .withQueryParam("name", equalTo(name))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

    private void stubForecast() {
        wireMock.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "latitude": 38.71, "longitude": -9.13,
                                  "timezone": "Europe/Lisbon", "utc_offset_seconds": 0,
                                  "current": {
                                    "time": "2026-11-13T17:00",
                                    "temperature_2m": 21.8,
                                    "relative_humidity_2m": 65,
                                    "apparent_temperature": 24.2,
                                    "is_day": 1, "precipitation": 0.0, "rain": 0.0,
                                    "weather_code": 2, "wind_speed_10m": 12.4
                                  },
                                  "daily": {
                                    "time": ["2026-11-13", "2026-11-14"],
                                    "weather_code": [2, 61],
                                    "temperature_2m_max": [25.1, 21.0],
                                    "temperature_2m_min": [17.2, 16.0],
                                    "precipitation_probability_max": [10, 60],
                                    "sunrise": ["2026-11-13T06:52", "2026-11-14T06:53"],
                                    "sunset": ["2026-11-13T17:21", "2026-11-14T17:20"]
                                  }
                                }""")));
    }
}