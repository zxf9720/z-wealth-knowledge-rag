package com.shawn.wealth.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AgentKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String complianceRequestedTopic;

    public AgentKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.kafka.topics.compliance-requested}") String complianceRequestedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.complianceRequestedTopic = complianceRequestedTopic;
    }

    public void publishComplianceRequested(String requestId,
                                           String sessionId,
                                           String customerId,
                                           String question,
                                           String policyAnswer) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "requestId", requestId,
                    "sessionId", sessionId,
                    "customerId", customerId,
                    "question", question,
                    "policyAnswer", policyAnswer
            ));

            kafkaTemplate.send(complianceRequestedTopic, requestId, payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish compliance requested event", e);
        }
    }
}