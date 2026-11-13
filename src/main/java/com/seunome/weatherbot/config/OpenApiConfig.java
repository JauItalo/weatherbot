package com.seunome.weatherbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherBotOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Weather Bot API")
                .description("Previsão do tempo de qualquer cidade do mundo, "
                        + "com dados da Open-Meteo.")
                .version("1.0.0"));
    }
}