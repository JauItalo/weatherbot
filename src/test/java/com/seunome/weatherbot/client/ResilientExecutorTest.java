package com.seunome.weatherbot.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.seunome.weatherbot.config.ResilienceConfig;
import com.seunome.weatherbot.exception.WeatherApiUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

class ResilientExecutorTest {

    private ResilientExecutor executor;
    private AtomicInteger calls;

    @BeforeEach
    void setUp() {
        calls = new AtomicInteger();
        executor = new ResilientExecutor(
                CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                        .slidingWindowSize(4)
                        .minimumNumberOfCalls(4)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .recordException(ResilienceConfig::isTransient)
                        .build()),
                RetryRegistry.of(RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(1))
                        .retryOnException(ResilienceConfig::isTransient)
                        .build()));
    }

    @Test
    void deveTentarNovamenteAteSucesso() {
        Supplier<String> flaky = () -> {
            if (calls.incrementAndGet() < 3) {
                throw new ResourceAccessException("timeout");
            }
            return "ok";
        };

        assertEquals("ok", executor.execute("t", flaky));
        assertEquals(3, calls.get());
    }

    @Test
    void naoDeveTentarNovamenteEmErro4xx() {
        Supplier<String> badRequest = () -> {
            calls.incrementAndGet();
            throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                    HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        };

        assertThrows(HttpClientErrorException.class, () -> executor.execute("t", badRequest));
        assertEquals(1, calls.get());
    }

    @Test
    void deveAbrirCircuitoAposFalhasEBloquearChamadas() {
        Supplier<String> failing = () -> {
            calls.incrementAndGet();
            throw new ResourceAccessException("timeout");
        };

        // 1ª execução: 3 tentativas, todas falham
        assertThrows(ResourceAccessException.class, () -> executor.execute("t", failing));
        assertEquals(3, calls.get());

        // 2ª execução: a 4ª falha abre o circuito e a tentativa seguinte é bloqueada
        assertThrows(WeatherApiUnavailableException.class, () -> executor.execute("t", failing));
        int chamadasAteAbrir = calls.get();

        // 3ª execução: circuito aberto, a chamada nem chega a executar
        assertThrows(WeatherApiUnavailableException.class, () -> executor.execute("t", failing));
        assertEquals(chamadasAteAbrir, calls.get());
    }
}