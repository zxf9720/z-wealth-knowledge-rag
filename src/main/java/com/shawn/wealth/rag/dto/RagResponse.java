package com.shawn.wealth.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response object for RAG queries.
 */
@Schema(name = "RagResponse", description = "Response returned by the RAG endpoint")
public record RagResponse(

        @Schema(
                description = "Operation status",
                example = "SUCCESS",
                allowableValues = {"SUCCESS", "NO_RESULT", "ERROR"}
        )
        String status,

        @Schema(
                description = "Generated answer from the LLM",
                example = "Hi! A TFSA is a tax-free savings account in Canada."
        )
        String answer,

        @Schema(
                description = "Retrieved source chunks used for answering"
        )
        List<com.shawn.wealth.rag.rag.dto.SourceItem> sources,

        @Schema(
                description = "Optional message, mainly used when no result is found",
                example = "No relevant documents matched the query."
        )
        String message
) {
}