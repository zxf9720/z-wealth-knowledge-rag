package com.shawn.wealth.rag.dto.external;

public record ComplianceCheckResponse(
        String status,
        String decision,
        String reason,
        String explanation,
        String explanationSource
) {
}
