package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.rag.dto.RagRequest;
import com.shawn.wealth.rag.rag.dto.RagResponse;
import com.shawn.wealth.rag.dto.external.ComplianceCheckRequest;
import com.shawn.wealth.rag.dto.external.ComplianceCheckResponse;
import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import com.shawn.wealth.rag.dto.agent.WealthComplianceRequest;
import com.shawn.wealth.rag.dto.agent.WealthComplianceResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Orchestrates RAG, customer data, and compliance review services.
 */
@Service
public class WealthComplianceAgentService {

    private final RagService ragService;
    private final CustomerDataClient customerDataClient;
    private final ComplianceReviewClient complianceReviewClient;

    public WealthComplianceAgentService(RagService ragService,
                                        CustomerDataClient customerDataClient,
                                        ComplianceReviewClient complianceReviewClient) {
        this.ragService = ragService;
        this.customerDataClient = customerDataClient;
        this.complianceReviewClient = complianceReviewClient;
    }

    public WealthComplianceResponse review(WealthComplianceRequest request) {
        validateRequest(request);

        CustomerProfileResponse customer =
                customerDataClient.getCustomerProfile(request.customerId());

        RagResponse ragResponse =
                ragService.ask(new RagRequest(request.sessionId(), request.question()));

        ComplianceCheckResponse complianceResponse =
                complianceReviewClient.checkCompliance(
                        new ComplianceCheckRequest(ragResponse.answer(), customer)
                );

        String finalAnswer = buildFinalAnswer(ragResponse, customer, complianceResponse);

        return new WealthComplianceResponse(
                "SUCCESS",
                customer,
                ragResponse.answer(),
                ragResponse.sources() == null ? Collections.emptyList() : ragResponse.sources(),
                complianceResponse,
                finalAnswer
        );
    }

    private void validateRequest(WealthComplianceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException("customerId must not be empty");
        }
        if (request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("question must not be empty");
        }
    }

    private String buildFinalAnswer(RagResponse ragResponse,
                                    CustomerProfileResponse customer,
                                    ComplianceCheckResponse complianceResponse) {
        return """
                Policy Answer:
                %s

                Customer:
                %s, risk level: %s, KYC: %s

                Compliance Decision:
                %s

                Reason:
                %s
                """.formatted(
                ragResponse.answer(),
                customer.customerId(),
                customer.riskLevel(),
                customer.kycStatus(),
                complianceResponse.decision(),
                complianceResponse.reason()
        );
    }
}