package com.shawn.wealth.rag.history;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "agent_history")
public class AgentHistoryDocument {

    @Id
    private String id;

    private String sessionId;
    private String requestId;
    private String intent;
    private String userMessage;
    private String assistantAnswer;
    private String status;
    private Instant createdAt;

    public AgentHistoryDocument() {
    }

    public AgentHistoryDocument(String sessionId,
                                String requestId,
                                String intent,
                                String userMessage,
                                String assistantAnswer,
                                String status) {
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.intent = intent;
        this.userMessage = userMessage;
        this.assistantAnswer = assistantAnswer;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIntent() {
        return intent;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getAssistantAnswer() {
        return assistantAnswer;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}