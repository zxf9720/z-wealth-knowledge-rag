package com.shawn.wealth.rag.dto.agent;

public record AgentOrchestrationRequest(
        String sessionId,
        String message,
        String customerId,
        String textA,
        String textB
) {
}