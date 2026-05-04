package com.shawn.wealth.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp")
public class McpDemoController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider toolCallbackProvider;

    public McpDemoController(ChatClient.Builder chatClientBuilder,
                             ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder.build();
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "What is the current TFSA interest rate?") String message) {
        return chatClient.prompt()
                .system("""
                        You are a banking assistant.
                        If the user asks about interest rates, use the available MCP tool.
                        Answer briefly.
                        """)
                .user(message)
                .toolCallbacks(toolCallbackProvider)
                .call()
                .content();
    }
}