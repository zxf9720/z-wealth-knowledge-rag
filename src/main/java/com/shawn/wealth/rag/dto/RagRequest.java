package com.shawn.wealth.rag.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request object for RAG queries.
 */
@Schema(name = "RagRequest", description = "Request payload for retrieval-augmented generation")
public record RagRequest(

        @Schema(
                description = "Unique conversation identifier used to isolate chat memory",
                example = "test-session-1"
        )
        String sessionId,

        @Schema(
                description = "User question to be answered using retrieved context",
                example = "What is TFSA?"
        )
        String question
) {
}
