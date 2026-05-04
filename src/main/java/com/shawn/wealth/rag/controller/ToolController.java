package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.tool.CompareRequest;
import com.shawn.wealth.rag.dto.tool.CompareResponse;
import com.shawn.wealth.rag.dto.tool.SummarizeRequest;
import com.shawn.wealth.rag.dto.tool.SummarizeResponse;
import com.shawn.wealth.rag.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tool APIs", description = "Endpoints for lightweight tool calling")
@RestController
@RequestMapping("/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @Operation(
            summary = "Summarize text",
            description = "Summarize input text using the LLM.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Summary generated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SummarizeResponse.class),
                                    examples = @ExampleObject(
                                            name = "Summarize Success",
                                            value = """
                                                    {
                                                      "status": "SUCCESS",
                                                      "summary": "A TFSA is a Canadian account that allows investments to grow tax-free."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/summarize")
    public SummarizeResponse summarize(@RequestBody SummarizeRequest request) {
        return toolService.summarize(request.text());
    }

    @Operation(
            summary = "Compare two texts",
            description = "Compare two text inputs using the LLM.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comparison generated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CompareResponse.class),
                                    examples = @ExampleObject(
                                            name = "Compare Success",
                                            value = """
                                                    {
                                                      "status": "SUCCESS",
                                                      "comparison": "TFSA provides tax-free growth, while RRSP offers tax-deductible contributions but taxable withdrawals."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/compare")
    public CompareResponse compare(@RequestBody CompareRequest request) {
        return toolService.compare(request.textA(), request.textB());
    }
}