package com.seunome.weatherbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @Size(max = 150, message = "A mensagem deve ter no máximo 150 caracteres.")
        @Schema(description = "Mensagem do usuário (vazia retorna as boas-vindas)",
                example = "Santa Cruz, Rio Grande do Norte",
                maxLength = 150)
        String message) {}
