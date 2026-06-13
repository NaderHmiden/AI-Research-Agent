package com.nader.aiagent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GroqConfig {
    @Value("${groq.api-key")
    private String apiKey;
    @Value("${groq.base-url")
    private String baseUrl;
    @Value("${groq.version")
    private String groqVersion;

    @Bean
    public WebClient GroqWebClient(){
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", groqVersion)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }



}
