package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.external.ComplianceCheckRequest;
import com.shawn.wealth.rag.dto.external.ComplianceCheckResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Non-blocking HTTP client for Agent C.
 *
 * The request is sent asynchronously and the thread is released while waiting
 * for Agent C to return the compliance decision.
 */
@Service
public class NonBlockingComplianceReviewClient {

    private final WebClient webClient;

    public NonBlockingComplianceReviewClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.compliance-review.base-url}") String baseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<ComplianceCheckResponse> checkCompliance(ComplianceCheckRequest request) {
        return webClient.post()
                .uri("/compliance/check")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ComplianceCheckResponse.class);
    }
}