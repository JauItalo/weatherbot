package com.seunome.weatherbot.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WmoCodeMapperTest {

    @ParameterizedTest
    @CsvSource({
            "1, Predominantemente limpo",
            "2, Parcialmente nublado",
            "3, Nublado",
            "45, Neblina",
            "53, Garoa",
            "61, Chuva fraca",
            "65, Chuva forte",
            "73, Neve moderada",
            "82, Pancadas de chuva violentas",
            "95, Trovoada",
            "99, Trovoada com granizo"
    })
    void deveTraduzirCodigoParaDescricao(int code, String esperado) {
        assertEquals(esperado, WmoCodeMapper.map(code, true).description());
    }

    @Test
    void ceuLimpoDeveMudarEmojiEntreDiaENoite() {
        assertEquals("☀️", WmoCodeMapper.map(0, true).emoji());
        assertEquals("🌙", WmoCodeMapper.map(0, false).emoji());
    }

    @Test
    void codigoNuloOuDesconhecidoDeveRetornarCondicaoDesconhecida() {
        assertEquals("Condição desconhecida", WmoCodeMapper.map(null, true).description());
        assertEquals("Condição desconhecida", WmoCodeMapper.map(12345, true).description());
    }
}