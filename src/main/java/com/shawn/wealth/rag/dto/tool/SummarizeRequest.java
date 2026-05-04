package com.shawn.wealth.rag.dto.tool;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SummarizeRequest", description = "Request payload for summarize tool")
public record SummarizeRequest(

        @Schema(
                description = "Text content to summarize",
                example = "A TFSA is a tax-free savings account in Canada. It allows tax-free growth on investments."
        )
        String text
) {
}