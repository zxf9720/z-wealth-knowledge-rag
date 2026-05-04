package com.shawn.wealth.rag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat APIs", description = "Endpoints for simple LLM chat")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Operation(
            summary = "Ask a chat question",
            description = "Send a simple prompt to the LLM and return a plain text response.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response from the LLM",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(
                                            name = "Chat Response Example",
                                            value = "Hi! A TFSA is a tax-free savings account in Canada."
                                    )
                            )
                    )
            }
    )
    @GetMapping("/ask")
    public String ask(@Parameter(
            description = "User input message sent to the LLM",
            example = "What is TFSA?"
            )@RequestParam(defaultValue = "Tell me a joke.") String message) {

        return chatClient.prompt()
                .system("You are a banking assistant. Answer briefly and start with 'Hi!'.")
                .user(message)
                .call()
                .content();
    }
}
