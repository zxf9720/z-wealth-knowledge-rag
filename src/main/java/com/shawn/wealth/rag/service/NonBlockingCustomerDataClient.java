package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Non-blocking HTTP client for Agent B.
 *
 * This client uses WebClient instead of RestTemplate.
 * WebClient does not block the request thread while waiting for the response.
 */
@Service
public class NonBlockingCustomerDataClient {

    private final WebClient webClient;

    public NonBlockingCustomerDataClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.customer-data.base-url}") String baseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<CustomerProfileResponse> getCustomerProfile(String customerId) {
        return webClient.get()
                .uri("/customers/{customerId}", customerId)
                .retrieve()
                .bodyToMono(CustomerProfileResponse.class);
    }
}