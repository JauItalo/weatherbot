package com.seunome.weatherbot.exception;

public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String city) {
        super("Cidade não encontrada: " + city);
    }
}