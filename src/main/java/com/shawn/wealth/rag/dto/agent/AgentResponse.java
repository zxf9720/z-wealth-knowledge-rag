package com.shawn.wealth.rag.dto.agent;

import com.shawn.wealth.rag.rag.dto.SourceItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "AgentResponse", description = "Unified response returned by the agent endpoint")
public record AgentResponse(

        @Schema(
                description = "Operation status",
                example = "SUCCESS"
        )
        String status,

        @Schema(
                description = "Detected user intent",
                example = "COMPARE"
        )
        IntentType intent,

        @Schema(
                description = "Selected internal tool",
                example = "COMPARE"
        )
        ToolType selectedTool,

        @Schema(
                description = "Final response content",
                example = "TFSA provides tax-free growth, while RRSP offers tax-deductible contributions but taxable withdrawals."
        )
        String answer,

        @Schema(
                description = "Optional retrieved sources, mainly used for RAG responses"
        )
        List<SourceItem> sources,

        @Schema(
                description = "Optional message for no-result or validation scenarios",
                example = "No relevant documents matched the query."
        )
        String message
) {
}
