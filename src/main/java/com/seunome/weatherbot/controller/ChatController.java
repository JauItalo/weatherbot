package com.seunome.weatherbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seunome.weatherbot.dto.ChatRequest;
import com.seunome.weatherbot.dto.ChatResponse;
import com.seunome.weatherbot.dto.ChatResponseType;
import com.seunome.weatherbot.formatter.WeatherMessageFormatter;
import com.seunome.weatherbot.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/welcome")
    public ChatResponse welcome() {
        return new ChatResponse(ChatResponseType.WELCOME, WeatherMessageFormatter.WELCOME, null);
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.handle(request.message());
    }
}