package com.seunome.weatherbot.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.seunome.weatherbot.config.CacheConfig;
import com.seunome.weatherbot.dto.GeocodingResponse;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.exception.WeatherApiUnavailableException;

@Component
public class GeocodingClient {

    private static final Logger log = LoggerFactory.getLogger(GeocodingClient.class);

    private final RestClient restClient;

    public GeocodingClient(@Qualifier("geocodingRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(
            cacheNames = CacheConfig.GEOCODING_CACHE,
            key = "#cityName.trim().toLowerCase()",
            unless = "#result.isEmpty()")
    public List<GeocodingResult> searchCity(String cityName) {
        log.info("Consultando geocoding na Open-Meteo: {}", cityName);
        try {
            GeocodingResponse response = restClient.get()
                    .uri(uri -> uri.path("/v1/search")
                            .queryParam("name", "{name}")
                            .queryParam("count", 10)
                            .queryParam("language", "pt")
                            .queryParam("format", "json")
                            .build(cityName))
                    .retrieve()
                    .body(GeocodingResponse.class);

            if (response == null || response.results() == null) {
                return List.of();
            }
            return response.results();

        } catch (RestClientException e) {
            throw new WeatherApiUnavailableException(
                    "Serviço de geolocalização indisponível no momento.", e);
        }
    }
}