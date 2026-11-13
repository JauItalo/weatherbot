package com.seunome.weatherbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRequest(
        @Schema(description = "Mensagem do usuário",
                example = "Santa Cruz, Rio Grande do Norte")
        String message) {}