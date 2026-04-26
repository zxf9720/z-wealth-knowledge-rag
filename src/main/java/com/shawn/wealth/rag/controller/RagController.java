package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieval-augmented generation queries.
 */
@Tag(name = "RAG APIs", description = "Endpoints for retrieval-augmented question answering")
@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(
            summary = "Ask a RAG question",
            description = "Retrieve relevant context from the vector database and generate an answer using the LLM.",
            responses = {

                    // SUCCESS CASE
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful RAG response",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RagResponse.class),
                                    examples = @ExampleObject(
                                            name = "SUCCESS",
                                            value = """
                                                    {
                                                      "status": "SUCCESS",
                                                      "answer": "Hi! A TFSA is a tax-free savings account in Canada.",
                                                      "sources": [
                                                        {
                                                          "content": "A TFSA is a tax-free savings account in Canada.",
                                                          "metadata": {
                                                            "documentId": "550e8400-e29b-41d4-a716-446655440000",
                                                            "fileName": "tfsa.txt",
                                                            "contentType": "text/plain",
                                                            "chunkStart": 0,
                                                            "chunkEnd": 55
                                                          }
                                                        }
                                                      ],
                                                      "message": null
                                                    }
                                                    """
                                    )
                            )
                    ),

                    // NO RESULT CASE
                    @ApiResponse(
                            responseCode = "200",
                            description = "No relevant documents found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RagResponse.class),
                                    examples = @ExampleObject(
                                            name = "NO_RESULT",
                                            value = """
                                                    {
                                                      "status": "NO_RESULT",
                                                      "answer": "I could not find relevant information in the knowledge base.",
                                                      "sources": [],
                                                      "message": "No relevant documents matched the query."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/ask")
    public RagResponse ask(@RequestBody RagRequest request) {
        return ragService.ask(request);
    }
}