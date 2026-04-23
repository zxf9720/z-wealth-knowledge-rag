package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.agent.AgentRequest;
import com.shawn.wealth.rag.dto.agent.AgentResponse;
import com.shawn.wealth.rag.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Agent APIs", description = "Endpoints for intent routing, tool selection, and internal invocation")
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @Operation(
            summary = "Ask the agent",
            description = "Route user intent, select the appropriate internal tool, and invoke it internally.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Agent response returned successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AgentResponse.class),
                                    examples = @ExampleObject(
                                            name = "Agent RAG Response",
                                            value = """
                                                    {
                                                      "status": "SUCCESS",
                                                      "intent": "RAG",
                                                      "selectedTool": "RAG",
                                                      "answer": "Hi! A TFSA is a tax-free savings account in Canada.",
                                                      "sources": [
                                                        {
                                                          "content": "A TFSA is a tax-free savings account in Canada.",
                                                          "metadata": {
                                                            "documentId": "550e8400-e29b-41d4-a716-446655440000",
                                                            "fileName": "tfsa.txt"
                                                          }
                                                        }
                                                      ],
                                                      "message": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/ask")
    public AgentResponse ask(@RequestBody AgentRequest request) {
        return agentService.ask(request);
    }
}