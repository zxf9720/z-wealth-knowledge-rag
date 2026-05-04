package com.shawn.wealth.rag.dto.agent;

import com.shawn.wealth.rag.dto.external.ComplianceCheckResponse;
import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import com.shawn.wealth.rag.rag.dto.SourceItem;

import java.util.List;

public record NonBlockingComplianceResponse(
        String status,
        String customerId,
        CustomerProfileResponse customer,
        String policyAnswer,
        List<SourceItem> sources,
        ComplianceCheckResponse compliance,
        String finalAnswer
) {
}
