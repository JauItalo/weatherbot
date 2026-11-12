package com.seunome.weatherbot.mapper;

public final class WmoCodeMapper {

    private static final WeatherCondition UNKNOWN =
            new WeatherCondition("Condição desconhecida", "❓");

    private WmoCodeMapper() {}

    public static WeatherCondition map(Integer code, boolean isDay) {
        if (code == null) {
            return UNKNOWN;
        }

        return switch (code) {
            case 0 -> isDay
                    ? new WeatherCondition("Céu limpo", "☀️")
                    : new WeatherCondition("Céu limpo", "🌙");
            case 1 -> new WeatherCondition("Predominantemente limpo", "🌤️");
            case 2 -> new WeatherCondition("Parcialmente nublado", "⛅");
            case 3 -> new WeatherCondition("Nublado", "☁️");
            case 45, 48 -> new WeatherCondition("Neblina", "🌫️");
            case 51, 53, 55 -> new WeatherCondition("Garoa", "🌦️");
            case 56, 57 -> new WeatherCondition("Garoa congelante", "🌧️");
            case 61 -> new WeatherCondition("Chuva fraca", "🌧️");
            case 63 -> new WeatherCondition("Chuva moderada", "🌧️");
            case 65 -> new WeatherCondition("Chuva forte", "🌧️");
            case 66, 67 -> new WeatherCondition("Chuva congelante", "🌧️");
            case 71 -> new WeatherCondition("Neve fraca", "🌨️");
            case 73 -> new WeatherCondition("Neve moderada", "🌨️");
            case 75 -> new WeatherCondition("Neve forte", "❄️");
            case 77 -> new WeatherCondition("Grãos de neve", "🌨️");
            case 80 -> new WeatherCondition("Pancadas de chuva fracas", "🌦️");
            case 81 -> new WeatherCondition("Pancadas de chuva moderadas", "🌧️");
            case 82 -> new WeatherCondition("Pancadas de chuva violentas", "⛈️");
            case 85, 86 -> new WeatherCondition("Pancadas de neve", "🌨️");
            case 95 -> new WeatherCondition("Trovoada", "⛈️");
            case 96, 99 -> new WeatherCondition("Trovoada com granizo", "⛈️");
            default -> UNKNOWN;
        };
    }
}