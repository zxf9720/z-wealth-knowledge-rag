package com.shawn.wealth.rag.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    private static final int MAX_MESSAGES = 20;

    @Bean
    public InMemoryChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    /**
     * ChatMemory maintains conversation context by storing previous messages between the user and the model.
     * MessageWindowChatMemory with InMemoryChatMemoryRepository is used with a limit of around 20 messages
     * to control token usage and keep sessions isolated.
     *
     * Around 20（10-30） messages is usually enough to keep recent context while controlling token usage.
     * If longer history is needed, we can use summarization instead of increasing the window size.
     *
     * @return  ChatMemory instance for managing conversation context
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(MAX_MESSAGES)
                .build();
    }
}
