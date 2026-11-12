package com.seunome.weatherbot.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.seunome.weatherbot.dto.ForecastResponse;
import com.seunome.weatherbot.exception.WeatherApiUnavailableException;

@Component
public class ForecastClient {

    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
            + "is_day,precipitation,rain,weather_code,wind_speed_10m";

    private static final String DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,"
            + "precipitation_probability_max,sunrise,sunset";

    private final RestClient restClient;

    public ForecastClient(@Qualifier("forecastRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ForecastResponse getForecast(double latitude, double longitude) {
        try {
            ForecastResponse response = restClient.get()
                    .uri(uri -> uri.path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", CURRENT_FIELDS)
                            .queryParam("daily", DAILY_FIELDS)
                            .queryParam("timezone", "auto")
                            .queryParam("forecast_days", 7)
                            .build())
                    .retrieve()
                    .body(ForecastResponse.class);

            if (response == null || response.current() == null || response.daily() == null) {
                throw new WeatherApiUnavailableException(
                        "A API meteorológica retornou uma resposta vazia.", null);
            }
            return response;

        } catch (RestClientException e) {
            throw new WeatherApiUnavailableException(
                    "Serviço meteorológico indisponível no momento.", e);
        }
    }
}