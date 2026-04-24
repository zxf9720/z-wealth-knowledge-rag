package com.shawn.wealth.rag.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Request object for ingesting raw text into the knowledge base.
 */
@Schema(name = "TextIngestRequest", description = "Request payload for raw text ingestion")
public record TextIngestRequest(

        @Schema(
                description = "Raw text content to be chunked, embedded, and stored in the vector database",
                example = "A TFSA is a tax-free savings account in Canada."
        )
        String text,

        @Schema(
                description = "Logical file name used for display and metadata purposes",
                example = "tfsa.txt"
        )
        String fileName,

        @Schema(
                description = "Optional metadata associated with the document",
                example = "{\"category\":\"banking\",\"source\":\"manual\",\"region\":\"CA\"}"
        )
        Map<String, Object> metadata
) {
}