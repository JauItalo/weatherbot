package com.seunome.weatherbot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.seunome.weatherbot.client.ForecastClient;
import com.seunome.weatherbot.client.GeocodingClient;
import com.seunome.weatherbot.dto.CurrentConditions;
import com.seunome.weatherbot.dto.CurrentWeather;
import com.seunome.weatherbot.dto.DailyForecast;
import com.seunome.weatherbot.dto.DailyForecastItem;
import com.seunome.weatherbot.dto.ForecastResponse;
import com.seunome.weatherbot.dto.GeocodingResult;
import com.seunome.weatherbot.dto.LocationInfo;
import com.seunome.weatherbot.dto.WeatherResponse;
import com.seunome.weatherbot.exception.CityNotFoundException;
import com.seunome.weatherbot.mapper.WeatherCondition;
import com.seunome.weatherbot.mapper.WmoCodeMapper;

@Service
public class WeatherService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    private final GeocodingClient geocodingClient;
    private final ForecastClient forecastClient;

    public WeatherService(GeocodingClient geocodingClient, ForecastClient forecastClient) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
    }

    public WeatherResponse getWeatherByCity(String cityName) {
        List<GeocodingResult> results = geocodingClient.searchCity(cityName);

        if (results.isEmpty()) {
            throw new CityNotFoundException(cityName);
        }

        // Por enquanto usamos o primeiro resultado.
        // Na Fase 5 vamos tratar cidades homônimas (desambiguação).
        return getWeatherByLocation(results.get(0));
    }

    public WeatherResponse getWeatherByLocation(GeocodingResult location) {
        ForecastResponse forecast = forecastClient.getForecast(
                location.latitude(), location.longitude());

        LocationInfo locationInfo = new LocationInfo(
                location.name(),
                location.admin1(),
                location.country(),
                forecast.timezone(),
                location.latitude(),
                location.longitude());

        return new WeatherResponse(
                locationInfo,
                buildCurrent(forecast.current(), forecast.daily()),
                buildDaily(forecast.daily()));
    }

    private CurrentConditions buildCurrent(CurrentWeather current, DailyForecast daily) {
        boolean isDay = current.isDay() != null && current.isDay() == 1;
        WeatherCondition condition = WmoCodeMapper.map(current.weatherCode(), isDay);

        return new CurrentConditions(
                round(current.temperature()),
                round(current.apparentTemperature()),
                current.relativeHumidity(),
                round(current.windSpeed()),
                at(daily.precipitationProbabilityMax(), 0),   // probabilidade de chuva de hoje
                condition.description(),
                condition.emoji(),
                formatTime(at(daily.sunrise(), 0)),
                formatTime(at(daily.sunset(), 0)));
    }

    private List<DailyForecastItem> buildDaily(DailyForecast daily) {
        List<DailyForecastItem> items = new ArrayList<>();
        List<String> dates = daily.time();

        if (dates == null) {
            return items;
        }

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = LocalDate.parse(dates.get(i));
            WeatherCondition condition = WmoCodeMapper.map(at(daily.weatherCode(), i), true);

            items.add(new DailyForecastItem(
                    date,
                    dayOfWeekName(date),
                    condition.description(),
                    condition.emoji(),
                    round(at(daily.temperatureMax(), i)),
                    round(at(daily.temperatureMin(), i)),
                    at(daily.precipitationProbabilityMax(), i)));
        }
        return items;
    }

    // ---------- Métodos auxiliares ----------

    private static String dayOfWeekName(LocalDate date) {
        String name = date.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String formatTime(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        return LocalDateTime.parse(isoDateTime).format(HOUR_MINUTE);
    }

    private static Integer round(Double value) {
        return value == null ? null : (int) Math.round(value);
    }

    private static <T> T at(List<T> list, int index) {
        return (list != null && index < list.size()) ? list.get(index) : null;
    }
}