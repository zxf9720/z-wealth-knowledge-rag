package com.shawn.wealth.rag.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AgentRequest", description = "Request payload for agent-style orchestration")
public record AgentRequest(

        @Schema(
                description = "Unique session identifier for chat memory isolation",
                example = "test-session-1"
        )
        String sessionId,

        @Schema(
                description = "User message used for intent routing",
                example = "Compare TFSA and RRSP"
        )
        String message,

        @Schema(
                description = "Optional first input for compare tool",
                example = "TFSA allows tax-free growth, but contributions are not tax deductible."
        )
        String textA,

        @Schema(
                description = "Optional second input for compare tool",
                example = "RRSP contributions are tax deductible, but withdrawals are taxable."
        )
        String textB
) {
}
