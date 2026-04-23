package com.shawn.wealth.rag.rag.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Retrieved source item used in the RAG response.
 */
@Schema(name = "SourceItem", description = "Retrieved source chunk returned with the RAG response")
public record SourceItem(

        @Schema(
                description = "Chunk text retrieved from the vector database",
                example = "A TFSA is a tax-free savings account in Canada."
        )
        String content,

        @Schema(
                description = "Chunk metadata",
                example = "{\"fileName\":\"tfsa.txt\",\"documentId\":\"550e8400-e29b-41d4-a716-446655440000\"}"
        )
        Map<String, Object> metadata
) {
}