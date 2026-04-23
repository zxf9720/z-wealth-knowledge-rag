package com.shawn.wealth.rag.dto.tool;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CompareRequest", description = "Request payload for compare tool")
public record CompareRequest(

        @Schema(
                description = "First text input for comparison",
                example = "TFSA allows tax-free growth, but contributions are not tax deductible."
        )
        String textA,

        @Schema(
                description = "Second text input for comparison",
                example = "RRSP contributions are tax deductible, but withdrawals are taxable."
        )
        String textB
) {
}