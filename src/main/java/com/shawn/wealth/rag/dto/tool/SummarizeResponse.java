package com.shawn.wealth.rag.dto.tool;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SummarizeResponse", description = "Response returned by summarize tool")
public record SummarizeResponse(

        @Schema(
                description = "Operation status",
                example = "SUCCESS"
        )
        String status,

        @Schema(
                description = "Summarized output",
                example = "A TFSA is a Canadian account that allows investments to grow tax-free."
        )
        String summary
) {
}