package com.shawn.wealth.rag.dto.external;

public record CustomerProfileResponse(
        String customerId,
        String name,
        int age,
        double annualIncome,
        String riskLevel,
        String investmentObjective,
        String kycStatus
) {
}
