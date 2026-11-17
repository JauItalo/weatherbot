package com.seunome.weatherbot.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ChatRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void mensagemNormalDeveSerValida() {
        assertTrue(validator.validate(new ChatRequest("Santa Cruz, RN")).isEmpty());
    }

    @Test
    void mensagemNulaOuVaziaDeveSerValidaPoisRetornaBoasVindas() {
        assertTrue(validator.validate(new ChatRequest(null)).isEmpty());
        assertTrue(validator.validate(new ChatRequest("")).isEmpty());
    }

    @Test
    void mensagemComMais_de_150_caracteresDeveSerInvalida() {
        var violations = validator.validate(new ChatRequest("a".repeat(151)));

        assertEquals(1, violations.size());
        assertEquals("A mensagem deve ter no máximo 150 caracteres.",
                violations.iterator().next().getMessage());
    }

    @Test
    void mensagemComExatamente_150_caracteresDeveSerValida() {
        assertTrue(validator.validate(new ChatRequest("a".repeat(150))).isEmpty());
    }
}
