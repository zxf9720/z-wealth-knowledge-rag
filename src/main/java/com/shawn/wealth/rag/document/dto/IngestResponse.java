package com.shawn.wealth.rag.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response object returned after a document ingestion or reindexing operation.
 */
@Schema(name = "IngestResponse", description = "Result of a document ingestion or reindexing request")
public record IngestResponse(

        @Schema(
                description = "Unique document identifier",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        String documentId,

        @Schema(
                description = "Operation status",
                example = "SUCCESS"
        )
        String status,

        @Schema(
                description = "Number of chunks written to the vector database",
                example = "4"
        )
        int chunks
) {
}