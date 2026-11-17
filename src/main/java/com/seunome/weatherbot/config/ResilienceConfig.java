package com.seunome.weatherbot.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordException(ResilienceConfig::isTransient)
                .build());
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(300))
                .retryOnException(ResilienceConfig::isTransient)
                .build());
    }

    /**
     * Falha transitória = vale tentar de novo e conta para abrir o circuito:
     * timeout/conexão (ResourceAccessException) e erros 5xx.
     * Erros 4xx (requisição errada) NÃO são transitórios.
     */
    public static boolean isTransient(Throwable ex) {
        return ex instanceof ResourceAccessException
                || ex instanceof HttpServerErrorException;
    }
}