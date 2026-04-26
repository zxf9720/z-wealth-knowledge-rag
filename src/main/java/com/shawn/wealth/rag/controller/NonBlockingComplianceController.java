package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.agent.NonBlockingComplianceRequest;
import com.shawn.wealth.rag.dto.agent.NonBlockingComplianceResponse;
import com.shawn.wealth.rag.service.NonBlockingComplianceOrchestrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Non-blocking orchestration endpoint.
 *
 * Returning Mono allows Spring to process the request asynchronously.
 * The servlet thread does not need to block while Agent A waits for
 * Agent B and Agent C.
 */
@RestController
@RequestMapping("/agent/nonblocking")
public class NonBlockingComplianceController {

    private final NonBlockingComplianceOrchestrationService orchestrationService;

    public NonBlockingComplianceController(
            NonBlockingComplianceOrchestrationService orchestrationService
    ) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/compliance/review")
    public Mono<NonBlockingComplianceResponse> review(
            @RequestBody NonBlockingComplianceRequest request
    ) {
        return orchestrationService.review(request);
    }
}