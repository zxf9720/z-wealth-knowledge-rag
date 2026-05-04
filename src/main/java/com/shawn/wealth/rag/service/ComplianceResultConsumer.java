package com.shawn.wealth.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.wealth.rag.history.AgentHistoryDocument;
import com.shawn.wealth.rag.history.AgentHistoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ComplianceResultConsumer {

    private final ObjectMapper objectMapper;
    private final AgentHistoryRepository historyRepository;
    private final ShortTermMemoryService shortTermMemoryService;

    public ComplianceResultConsumer(ObjectMapper objectMapper,
                                    AgentHistoryRepository historyRepository,
                                    ShortTermMemoryService shortTermMemoryService) {
        this.objectMapper = objectMapper;
        this.historyRepository = historyRepository;
        this.shortTermMemoryService = shortTermMemoryService;
    }

    @KafkaListener(topics = "${app.kafka.topics.compliance-completed}")
    public void consume(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);

            String requestId = json.get("requestId").asText();
            String sessionId = json.get("sessionId").asText();
            String finalAnswer = json.get("finalAnswer").asText();
            String status = json.get("status").asText();

            shortTermMemoryService.addMessage(sessionId, "ASSISTANT", finalAnswer);

            historyRepository.save(new AgentHistoryDocument(
                    sessionId,
                    requestId,
                    "COMPLIANCE_REVIEW_RESULT",
                    "Async compliance review completed",
                    finalAnswer,
                    status
            ));

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume compliance completed event", e);
        }
    }
}