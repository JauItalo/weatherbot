package com.seunome.weatherbot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenMeteoProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient geocodingRestClient(OpenMeteoProperties props) {
        return buildClient(props.geocodingUrl(), props);
    }

    @Bean
    public RestClient forecastRestClient(OpenMeteoProperties props) {
        return buildClient(props.forecastUrl(), props);
    }

    private RestClient buildClient(String baseUrl, OpenMeteoProperties props) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(props.connectTimeout());
        requestFactory.setReadTimeout(props.readTimeout());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}