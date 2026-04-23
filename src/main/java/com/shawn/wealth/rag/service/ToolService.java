package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.tool.CompareResponse;
import com.shawn.wealth.rag.dto.tool.SummarizeResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    private final ChatClient chatClient;

    public ToolService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SummarizeResponse summarize(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be empty");
        }

        String summary = chatClient.prompt()
                .system("You are a banking assistant. Summarize the content clearly and briefly.")
                .user("""
                        Summarize the following text in 2 to 3 concise sentences:

                        %s
                        """.formatted(text))
                .call()
                .content();

        return new SummarizeResponse("SUCCESS", summary);
    }

    public CompareResponse compare(String textA, String textB) {
        if (textA == null || textA.isBlank()) {
            throw new IllegalArgumentException("textA must not be empty");
        }
        if (textB == null || textB.isBlank()) {
            throw new IllegalArgumentException("textB must not be empty");
        }

        String comparison = chatClient.prompt()
                .system("You are a banking assistant. Compare the two inputs clearly and briefly.")
                .user("""
                        Compare the following two texts.
                        Focus on key similarities and differences.
                        Respond in 3 to 5 concise sentences.

                        Text A:
                        %s

                        Text B:
                        %s
                        """.formatted(textA, textB))
                .call()
                .content();

        return new CompareResponse("SUCCESS", comparison);
    }
}
