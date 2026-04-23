package com.shawn.wealth.rag.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object representing a stored document in the knowledge base.
 */
@Schema(name = "DocumentResponse", description = "Document metadata returned by document management APIs")
public record DocumentResponse(

        @Schema(
                description = "Unique document identifier",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        String id,

        @Schema(
                description = "Original file name or logical document name",
                example = "tfsa.txt"
        )
        String fileName,

        @Schema(
                description = "Document content type",
                example = "text/plain"
        )
        String contentType,

        @Schema(
                description = "Current document indexing status",
                example = "INDEXED"
        )
        String status,

        @Schema(
                description = "Document metadata",
                example = "{\"category\":\"banking\",\"source\":\"manual\"}"
        )
        Map<String, Object> metadata,

        @Schema(
                description = "Document creation timestamp",
                example = "2026-04-22T12:00:00"
        )
        LocalDateTime createdAt
) {
}