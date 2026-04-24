package com.shawn.wealth.rag.dto.external;

public record ComplianceCheckRequest(
        String policy,
        CustomerProfileResponse customer
) {
}
