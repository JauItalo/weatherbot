package com.seunome.weatherbot.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;

@Service
public class ChatService {

    private static final Set<String> GREETINGS = Set.of(
            "oi", "ola", "olá", "bom dia", "boa tarde", "boa noite", "menu", "ajuda", "start");

    private static final int MAX_OPTIONS = 5;

    private final GeocodingClient geocodingClient;
    private final WeatherService weatherService;
    private final WeatherMessageFormatter formatter;

    public ChatService(GeocodingClient geocodingClient,
                       WeatherService weatherService,
                       WeatherMessageFormatter formatter) {
        this.geocodingClient = geocodingClient;
        this.weatherService = weatherService;
        this.formatter = formatter;
    }

    public ChatResponse handle(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.trim();

        if (message.isEmpty() || GREETINGS.contains(message.toLowerCase())) {
            return new ChatResponse(ChatResponseType.WELCOME, WeatherMessageFormatter.WELCOME, null);
        }

        // "Santa Cruz, Rio Grande do Norte" -> cidade = "Santa Cruz", filtro = "Rio Grande do Norte"
        String city = message;
        String filter = null;
        int comma = message.indexOf(',');
        if (comma > 0) {
            city = message.substring(0, comma).trim();
            filter = message.substring(comma + 1).trim();
        }

        List<GeocodingResult> results = geocodingClient.searchCity(city);

        if (filter != null && !filter.isEmpty()) {
            results = applyFilter(results, filter);
        }

        if (results.isEmpty()) {
            return new ChatResponse(ChatResponseType.NOT_FOUND, formatter.formatNotFound(message), null);
        }

        if (isAmbiguous(results)) {
            List<GeocodingResult> options = results.stream().limit(MAX_OPTIONS).toList();
            return new ChatResponse(
                    ChatResponseType.DISAMBIGUATION,
                    formatter.formatDisambiguation(city, options),
                    null);
        }

        WeatherResponse weather = weatherService.getWeatherByLocation(results.get(0));
        return new ChatResponse(ChatResponseType.WEATHER, formatter.formatWeather(weather), weather);
    }

    /**
     * Ambígua = mais de um resultado E nenhum é claramente dominante.
     * Uma cidade é dominante se tem pelo menos 5x a população da segunda colocada
     * (ex.: "Lisboa", "São Paulo", "Paris" não precisam de desambiguação).
     */
    private boolean isAmbiguous(List<GeocodingResult> results) {
        if (results.size() == 1) {
            return false;
        }
        long first = population(results.get(0));
        long second = population(results.get(1));
        return !(first > 0 && first >= second * 5);
    }

    private List<GeocodingResult> applyFilter(List<GeocodingResult> results, String filter) {
        String f = normalize(filter);
        return results.stream()
                .filter(r -> matches(r.admin1(), f)
                        || matches(r.country(), f)
                        || (r.countryCode() != null && normalize(r.countryCode()).equals(f)))
                .toList();
    }

    private boolean matches(String field, String normalizedFilter) {
        return field != null && normalize(field).equals(normalizedFilter);
    }

    private long population(GeocodingResult r) {
        return r.population() == null ? 0 : r.population();
    }

    /** Remove acentos e ignora maiúsculas: "São Paulo" == "sao paulo". */
    private String normalize(String text) {
        String n = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.toLowerCase().trim();
    }
}