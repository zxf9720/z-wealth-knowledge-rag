package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.external.ComplianceCheckRequest;
import com.shawn.wealth.rag.dto.external.ComplianceCheckResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for compliance review service.
 */
@Service
public class ComplianceReviewClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ComplianceReviewClient(RestTemplate restTemplate,
                                  @Value("${services.compliance-review.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ComplianceCheckResponse checkCompliance(ComplianceCheckRequest request) {
        return restTemplate.postForObject(
                baseUrl + "/compliance/check",
                request,
                ComplianceCheckResponse.class
        );
    }
}