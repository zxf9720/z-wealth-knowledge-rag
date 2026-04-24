package com.shawn.wealth.rag.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "WealthComplianceRequest", description = "Request payload for wealth compliance orchestration")
public record WealthComplianceRequest(

        @Schema(description = "Session identifier for memory isolation", example = "test-session-1")
        String sessionId,

        @Schema(description = "Customer identifier", example = "C1001")
        String customerId,

        @Schema(description = "Policy or product question", example = "Is this customer eligible for this TFSA product?")
        String question
) {
}
