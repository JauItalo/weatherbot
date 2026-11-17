package com.seunome.weatherbot.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BrazilianStatesTest {

    @ParameterizedTest
    @CsvSource({
            "SP, São Paulo",
            "pb, Paraíba",
            "RN, Rio Grande do Norte",
            "DF, Distrito Federal",
            "' TO ', Tocantins"
    })
    void deveConverterSiglaEmNome(String uf, String esperado) {
        assertEquals(esperado, BrazilianStates.nameOf(uf).orElseThrow());
    }

    @Test
    void siglaInvalidaOuNulaDeveRetornarVazio() {
        assertTrue(BrazilianStates.nameOf("XX").isEmpty());
        assertTrue(BrazilianStates.nameOf("São Paulo").isEmpty());
        assertTrue(BrazilianStates.nameOf(null).isEmpty());
    }
}
