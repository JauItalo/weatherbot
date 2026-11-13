package com.seunome.weatherbot.controller;

import com.seunome.weatherbot.dto.ChatRequest;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;
import com.seunome.weatherbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Interface conversacional do bot")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Mensagem de boas-vindas")
    @GetMapping("/welcome")
    public ChatResponse welcome() {
        return new ChatResponse(ChatResponseType.WELCOME, WeatherMessageFormatter.WELCOME, null);
    }

    @Operation(
            summary = "Envia uma mensagem ao bot",
            description = "Aceita saudações, o nome de uma cidade ou 'cidade, estado/país' "
                    + "para resolver cidades homônimas. O campo 'type' indica o tipo de resposta: "
                    + "WELCOME, WEATHER, DISAMBIGUATION ou NOT_FOUND.")
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.handle(request.message());
    }
}