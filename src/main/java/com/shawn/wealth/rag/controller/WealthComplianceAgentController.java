package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.agent.WealthComplianceRequest;
import com.shawn.wealth.rag.dto.agent.WealthComplianceResponse;
import com.shawn.wealth.rag.service.WealthComplianceAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for orchestrated multi-service compliance workflow.
 */
@Tag(name = "Wealth Compliance Agent APIs", description = "Endpoints for multi-service compliance orchestration")
@RestController
@RequestMapping("/agent/compliance")
public class WealthComplianceAgentController {

    private final WealthComplianceAgentService wealthComplianceAgentService;

    public WealthComplianceAgentController(WealthComplianceAgentService wealthComplianceAgentService) {
        this.wealthComplianceAgentService = wealthComplianceAgentService;
    }

    @Operation(
            summary = "Review customer compliance",
            description = "Retrieve policy knowledge, load customer profile, and run compliance review."
    )
    @PostMapping("/review")
    public WealthComplianceResponse review(@RequestBody WealthComplianceRequest request) {
        return wealthComplianceAgentService.review(request);
    }
}
