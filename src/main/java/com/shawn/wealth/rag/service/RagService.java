package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.rag.dto.SourceItem;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieval-augmented generation.
 */
@Service
public class RagService {

    private static final int TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.70d;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    public RagService(ChatClient.Builder chatClientBuilder,
                      VectorStore vectorStore,
                      ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    public RagResponse ask(RagRequest request) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.question())
                        .topK(TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build()
        );

        if (documents == null || documents.isEmpty()) {
            return new RagResponse(
                    "NO_RESULT",
                    "I could not find relevant information in the knowledge base.",
                    Collections.emptyList(),
                    "No relevant documents matched the query."
            );
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String userPrompt = """
                Answer the question only based on the context below.
                If the answer is not in the context, say that the information is not available.

                Context:
                %s

                Question:
                %s
                """.formatted(context, request.question());

        String answer = chatClient.prompt()
                .system("You are a banking assistant. Answer briefly and start with 'Hi!'.")
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(request.sessionId())
                                .build()
                )
                .user(userPrompt)
                .call()
                .content();

        List<SourceItem> sources = documents.stream()
                .map(document -> new SourceItem(document.getText(), document.getMetadata()))
                .collect(Collectors.toList());

        return new RagResponse(
                "SUCCESS",
                answer,
                sources,
                null
        );
    }
}