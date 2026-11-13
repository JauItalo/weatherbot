package com.seunome.weatherbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.dto.CurrentConditions;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.LocationInfo;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherService weatherService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(geocodingClient, weatherService, new WeatherMessageFormatter());
    }

    @Test
    void mensagemVaziaDeveRetornarBoasVindas() {
        ChatResponse r = chatService.handle("   ");

        assertEquals(ChatResponseType.WELCOME, r.type());
        assertEquals(WeatherMessageFormatter.WELCOME, r.message());
        verifyNoInteractions(geocodingClient);
    }

    @Test
    void saudacaoDeveRetornarBoasVindasIgnorandoMaiusculas() {
        assertEquals(ChatResponseType.WELCOME, chatService.handle("Oi").type());
        assertEquals(ChatResponseType.WELCOME, chatService.handle("BOM DIA").type());
    }

    @Test
    void cidadeInexistenteDeveRetornarNotFound() {
        when(geocodingClient.searchCity("xyz")).thenReturn(List.of());

        ChatResponse r = chatService.handle("xyz");

        assertEquals(ChatResponseType.NOT_FOUND, r.type());
        assertNull(r.weather());
    }

    @Test
    void unicoResultadoDeveRetornarPrevisao() {
        GeocodingResult lisboa = cidade(1L, "Lisboa", "Lisboa", "Portugal", "PT", 500_000L);
        when(geocodingClient.searchCity("Lisboa")).thenReturn(List.of(lisboa));
        when(weatherService.getWeatherByLocation(lisboa)).thenReturn(weatherFake());

        ChatResponse r = chatService.handle("Lisboa");

        assertEquals(ChatResponseType.WEATHER, r.type());
        assertNotNull(r.weather());
    }

    @Test
    void cidadeDominanteNaoDeveSerAmbigua() {
        GeocodingResult grande = cidade(1L, "Paris", "Île-de-France", "França", "FR", 2_000_000L);
        GeocodingResult pequena = cidade(2L, "Paris", "Texas", "Estados Unidos", "US", 25_000L);
        when(geocodingClient.searchCity("Paris")).thenReturn(List.of(grande, pequena));
        when(weatherService.getWeatherByLocation(grande)).thenReturn(weatherFake());

        ChatResponse r = chatService.handle("Paris");

        assertEquals(ChatResponseType.WEATHER, r.type());
        verify(weatherService).getWeatherByLocation(grande);
    }

    @Test
    void cidadesParecidasDevemGerarDesambiguacao() {
        GeocodingResult a = cidade(1L, "Santa Cruz", "Rio Grande do Norte", "Brasil", "BR", 40_000L);
        GeocodingResult b = cidade(2L, "Santa Cruz", "Santa Cruz", "Bolívia", "BO", 35_000L);
        when(geocodingClient.searchCity("Santa Cruz")).thenReturn(List.of(a, b));

        ChatResponse r = chatService.handle("Santa Cruz");

        assertEquals(ChatResponseType.DISAMBIGUATION, r.type());
        assertNull(r.weather());
        verify(weatherService, never()).getWeatherByLocation(any());
    }

    @Test
    void filtroPorEstadoSemAcentoDeveEscolherACidade() {
        GeocodingResult a = cidade(1L, "Santa Cruz", "Rio Grande do Norte", "Brasil", "BR", 40_000L);
        GeocodingResult b = cidade(2L, "Santa Cruz", "Santa Cruz", "Bolívia", "BO", 35_000L);
        when(geocodingClient.searchCity("Santa Cruz")).thenReturn(List.of(a, b));
        when(weatherService.getWeatherByLocation(a)).thenReturn(weatherFake());

        ChatResponse r = chatService.handle("Santa Cruz, rio grande do norte");

        assertEquals(ChatResponseType.WEATHER, r.type());
        verify(weatherService).getWeatherByLocation(a);
    }

    @Test
    void filtroPorCodigoDePaisDeveFuncionar() {
        GeocodingResult a = cidade(1L, "Santa Cruz", "Rio Grande do Norte", "Brasil", "BR", 40_000L);
        GeocodingResult b = cidade(2L, "Santa Cruz", "Santa Cruz", "Bolívia", "BO", 35_000L);
        when(geocodingClient.searchCity("Santa Cruz")).thenReturn(List.of(a, b));
        when(weatherService.getWeatherByLocation(b)).thenReturn(weatherFake());

        ChatResponse r = chatService.handle("Santa Cruz, BO");

        assertEquals(ChatResponseType.WEATHER, r.type());
        verify(weatherService).getWeatherByLocation(b);
    }

    @Test
    void filtroSemCorrespondenciaDeveRetornarNotFound() {
        GeocodingResult a = cidade(1L, "Santa Cruz", "Rio Grande do Norte", "Brasil", "BR", 40_000L);
        when(geocodingClient.searchCity("Santa Cruz")).thenReturn(List.of(a));

        ChatResponse r = chatService.handle("Santa Cruz, Japão");

        assertEquals(ChatResponseType.NOT_FOUND, r.type());
    }

    // ---------- helpers ----------

    private GeocodingResult cidade(Long id, String nome, String estado,
                                   String pais, String codigoPais, Long populacao) {
        return new GeocodingResult(id, nome, 0.0, 0.0, codigoPais, pais, estado, "UTC", populacao);
    }

    private WeatherResponse weatherFake() {
        return new WeatherResponse(
                new LocationInfo("X", "Y", "Z", "UTC", 0.0, 0.0),
                new CurrentConditions(20, 20, 50, 10, 0, "Nublado", "☁️", "06:00", "18:00"),
                List.of());
    }
}