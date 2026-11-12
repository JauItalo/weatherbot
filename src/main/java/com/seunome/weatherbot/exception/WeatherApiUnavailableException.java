package com.seunome.weatherbot.exception;

public class WeatherApiUnavailableException extends RuntimeException {

    public WeatherApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}