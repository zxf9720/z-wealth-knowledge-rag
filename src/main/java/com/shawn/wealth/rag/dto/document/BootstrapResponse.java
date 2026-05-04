package com.shawn.wealth.rag.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BootstrapResponse", description = "Result of bootstrapping local documents into the vector database")
public record BootstrapResponse(
        @Schema(description = "Number of newly indexed documents", example = "3")
        int indexed,

        @Schema(description = "Number of existing documents replaced by file name", example = "2")
        int replaced
) {
}