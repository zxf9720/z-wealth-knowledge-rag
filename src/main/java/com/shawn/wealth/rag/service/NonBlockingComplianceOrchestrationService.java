package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.dto.agent.NonBlockingComplianceRequest;
import com.shawn.wealth.rag.dto.agent.NonBlockingComplianceResponse;
import com.shawn.wealth.rag.dto.external.ComplianceCheckRequest;
import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

/**
 * Non-blocking orchestration service in Agent A.
 *
 * This service demonstrates where non-blocking programming is useful:
 * Agent A coordinates multiple I/O operations:
 *
 * 1. RAG lookup
 * 2. Customer service call
 * 3. Compliance service call
 *
 * The external HTTP calls use WebClient and return Mono.
 * The existing RagService is blocking, so it is wrapped in Mono.fromCallable
 * and executed on boundedElastic to avoid blocking event-loop threads.
 */
@Service
public class NonBlockingComplianceOrchestrationService {

    private final RagService ragService;
    private final NonBlockingCustomerDataClient customerDataClient;
    private final NonBlockingComplianceReviewClient complianceReviewClient;

    public NonBlockingComplianceOrchestrationService(
            RagService ragService,
            NonBlockingCustomerDataClient customerDataClient,
            NonBlockingComplianceReviewClient complianceReviewClient
    ) {
        this.ragService = ragService;
        this.customerDataClient = customerDataClient;
        this.complianceReviewClient = complianceReviewClient;
    }

    public Mono<NonBlockingComplianceResponse> review(NonBlockingComplianceRequest request) {
        validate(request);

        /*
         * RagService is currently a blocking service.
         * We wrap it in Mono.fromCallable and run it on boundedElastic.
         *
         * boundedElastic is intended for blocking I/O work inside a reactive flow.
         */
        Mono<RagResponse> ragMono = Mono.fromCallable(() ->
                        ragService.ask(new RagRequest(request.sessionId(), request.question()))
                )
                .subscribeOn(Schedulers.boundedElastic());

        /*
         * Customer service call is fully non-blocking because it uses WebClient.
         */
        Mono<CustomerProfileResponse> customerMono =
                customerDataClient.getCustomerProfile(request.customerId());

        /*
         * Run RAG and customer lookup concurrently.
         * After both complete, call compliance service using the retrieved policy answer
         * and customer profile.
         */
        return Mono.zip(ragMono, customerMono)
                .flatMap(tuple -> {
                    RagResponse ragResponse = tuple.getT1();
                    CustomerProfileResponse customer = tuple.getT2();

                    ComplianceCheckRequest complianceRequest =
                            new ComplianceCheckRequest(ragResponse.answer(), customer);

                    return complianceReviewClient.checkCompliance(complianceRequest)
                            .map(complianceResponse -> {
                                String finalAnswer = buildFinalAnswer(
                                        ragResponse,
                                        customer,
                                        complianceResponse.decision(),
                                        complianceResponse.reason(),
                                        complianceResponse.explanation()
                                );

                                return new NonBlockingComplianceResponse(
                                        "SUCCESS",
                                        request.customerId(),
                                        customer,
                                        ragResponse.answer(),
                                        ragResponse.sources() == null
                                                ? Collections.emptyList()
                                                : ragResponse.sources(),
                                        complianceResponse,
                                        finalAnswer
                                );
                            });
                });
    }

    private void validate(NonBlockingComplianceRequest request) {
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
                                    String decision,
                                    String reason,
                                    String explanation) {
        return """
                Policy Answer:
                %s

                Customer:
                %s, risk level: %s, KYC: %s

                Compliance Decision:
                %s

                Reason:
                %s

                Explanation:
                %s
                """.formatted(
                ragResponse.answer(),
                customer.customerId(),
                customer.riskLevel(),
                customer.kycStatus(),
                decision,
                reason,
                explanation
        );
    }
}