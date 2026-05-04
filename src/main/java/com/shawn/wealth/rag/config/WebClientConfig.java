package com.shawn.wealth.rag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides a reusable WebClient builder.
 *
 * WebClient is non-blocking and is suitable for service-to-service calls
 * in orchestration flows where the application waits for external I/O.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}