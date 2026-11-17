package com.seunome.weatherbot.client;

import com.seunome.weatherbot.exception.WeatherApiUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResilientExecutor {

    private static final Logger log = LoggerFactory.getLogger(ResilientExecutor.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public ResilientExecutor(CircuitBreakerRegistry circuitBreakerRegistry,
                             RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    public <T> T execute(String name, Supplier<T> call) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        Retry retry = retryRegistry.retry(name);

        // O retry fica POR FORA: cada tentativa passa pelo circuit breaker.
        Supplier<T> decorated = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, call));

        try {
            return decorated.get();
        } catch (CallNotPermittedException e) {
            log.warn("Circuito '{}' aberto: chamada bloqueada sem consultar a API", name);
            throw new WeatherApiUnavailableException(
                    "Serviço temporariamente indisponível (circuito aberto).", e);
        }
    }
}