package com.shawn.wealth.rag.dto.agent;

public record NonBlockingComplianceRequest(
        String sessionId,
        String customerId,
        String question
) {
}
