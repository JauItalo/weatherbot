package com.seunome.weatherbot.service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.ChatOption;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;
import com.seunome.weatherbot.mapper.BrazilianStates;

@Service
public class ChatService {

    /** Saudações já normalizadas (sem acento, minúsculas, sem pontuação final). */
    private static final Set<String> GREETINGS = Set.of(
            "oi", "ola", "e ai", "bom dia", "boa tarde", "boa noite", "tudo bem",
            "menu", "ajuda", "start", "help");

    /** Uma cidade é dominante se tiver pelo menos 5x a população da segunda maior. */
    private static final int DOMINANCE_FACTOR = 5;

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

        // "Santa Cruz, Rio Grande do Norte" -> cidade = "Santa Cruz", filtro = "Rio Grande do Norte"
        String city = message;
        String filter = null;
        int comma = message.indexOf(',');
        if (comma > 0) {
            city = message.substring(0, comma).trim();
            filter = message.substring(comma + 1).trim();
        }

        // Vazio, "Oi", "Oi!" ou "Oi, tudo bem?": o trecho antes da vírgula é uma saudação.
        if (message.isEmpty() || isGreeting(city)) {
            return new ChatResponse(ChatResponseType.WELCOME, WeatherMessageFormatter.WELCOME, null, null);
        }

        List<GeocodingResult> results = geocodingClient.searchCity(city);

        if (filter != null && !filter.isEmpty()) {
            results = applyFilter(results, filter);
        }

        if (results.isEmpty()) {
            return new ChatResponse(ChatResponseType.NOT_FOUND, formatter.formatNotFound(message), null, null);
        }

        Optional<GeocodingResult> chosen = pickCity(results);

        if (chosen.isEmpty()) {
            List<GeocodingResult> options = results.stream()
                    .sorted(Comparator.<GeocodingResult>comparingLong(this::population).reversed())
                    .limit(MAX_OPTIONS)
                    .toList();
            return new ChatResponse(
                    ChatResponseType.DISAMBIGUATION,
                    formatter.formatDisambiguation(city, options),
                    null,
                    toChatOptions(city, options));
        }

        WeatherResponse weather = weatherService.getWeatherByLocation(chosen.get());
        return new ChatResponse(ChatResponseType.WEATHER, formatter.formatWeather(weather), weather, null);
    }

    /**
     * Escolhe a cidade sem pedir desambiguação quando possível:
     * - só um resultado: usa ele;
     * - a cidade de MAIOR população (não necessariamente a primeira da lista) é dominante
     *   se tiver pelo menos 5x a população da segunda maior (ex.: "Lisboa", "Paris");
     * - caso contrário (ou sem dados de população): vazio, e o bot pede para o usuário escolher.
     */
    private Optional<GeocodingResult> pickCity(List<GeocodingResult> results) {
        if (results.size() == 1) {
            return Optional.of(results.get(0));
        }
        List<GeocodingResult> byPopulation = results.stream()
                .sorted(Comparator.<GeocodingResult>comparingLong(this::population).reversed())
                .toList();
        long top = population(byPopulation.get(0));
        long second = population(byPopulation.get(1));
        // top >= second * 5, sem multiplicação (inteiros: equivale a top / 5 >= second)
        boolean dominant = top > 0 && top / DOMINANCE_FACTOR >= second;
        return dominant ? Optional.of(byPopulation.get(0)) : Optional.empty();
    }

    /**
     * Botões da desambiguação. A consulta reenviada é "cidade, estado" (ou "cidade, país" quando a
     * API não informa o estado), que o filtro do próprio chat já entende.
     */
    private List<ChatOption> toChatOptions(String city, List<GeocodingResult> options) {
        return options.stream()
                .map(r -> new ChatOption(
                        joinNonBlank(r.name(), r.admin1(), r.country()),
                        city + ", " + (isBlank(r.admin1()) ? r.countryCode() : r.admin1())))
                .toList();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String joinNonBlank(String... parts) {
        return java.util.Arrays.stream(parts)
                .filter(p -> !isBlank(p))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private boolean isGreeting(String text) {
        String n = normalize(text).replaceAll("[!?.\\s]+$", "");
        return GREETINGS.contains(n);
    }

    private List<GeocodingResult> applyFilter(List<GeocodingResult> results, String filter) {
        String f = normalize(filter);
        // "PB" -> "paraiba" (só vale para cidades do Brasil)
        String ufName = BrazilianStates.nameOf(filter).map(this::normalize).orElse(null);
        return results.stream()
                .filter(r -> matches(r.admin1(), f)
                        || matches(r.country(), f)
                        || (r.countryCode() != null && normalize(r.countryCode()).equals(f))
                        || (ufName != null && "BR".equalsIgnoreCase(r.countryCode())
                                && matches(r.admin1(), ufName)))
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