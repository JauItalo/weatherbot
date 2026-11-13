package com.seunome.weatherbot.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GEOCODING_CACHE = "geocoding";
    public static final String FORECAST_CACHE = "forecast";

    @Bean
    public CacheManager cacheManager(
            @Value("${weatherbot.cache.geocoding-ttl:24h}") Duration geocodingTtl,
            @Value("${weatherbot.cache.forecast-ttl:10m}") Duration forecastTtl) {

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build(GEOCODING_CACHE, geocodingTtl, 1_000),
                build(FORECAST_CACHE, forecastTtl, 500)));
        return manager;
    }

    private CaffeineCache build(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}