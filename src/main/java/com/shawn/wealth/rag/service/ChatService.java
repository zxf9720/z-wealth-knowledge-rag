package com.shawn.wealth.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String ask(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        return chatClient.prompt()
                .system("You are a banking assistant. Answer briefly and start with 'Hi!'.")
                .user(message)
                .call()
                .content();
    }
}
