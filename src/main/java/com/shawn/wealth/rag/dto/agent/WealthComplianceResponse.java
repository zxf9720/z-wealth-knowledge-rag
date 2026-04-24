package com.shawn.wealth.rag.dto.agent;

import com.shawn.wealth.rag.dto.external.ComplianceCheckResponse;
import com.shawn.wealth.rag.dto.external.CustomerProfileResponse;
import com.shawn.wealth.rag.rag.dto.SourceItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "WealthComplianceResponse", description = "Response returned by wealth compliance orchestration")
public record WealthComplianceResponse(

        @Schema(description = "Operation status", example = "SUCCESS")
        String status,

        @Schema(description = "Customer profile returned from customer data service")
        CustomerProfileResponse customer,

        @Schema(description = "RAG-generated policy answer")
        String policyAnswer,

        @Schema(description = "Sources retrieved by RAG")
        List<SourceItem> sources,

        @Schema(description = "Compliance decision returned from compliance review service")
        ComplianceCheckResponse compliance,

        @Schema(description = "Final user-facing answer")
        String finalAnswer
) {
}
