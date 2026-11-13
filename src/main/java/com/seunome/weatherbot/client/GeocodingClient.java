package com.seunome.weatherbot.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.seunome.weatherbot.dto.GeocodingResponse;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.exception.WeatherApiUnavailableException;

@Component
public class GeocodingClient {

    private final RestClient restClient;

    public GeocodingClient(@Qualifier("geocodingRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<GeocodingResult> searchCity(String cityName) {
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