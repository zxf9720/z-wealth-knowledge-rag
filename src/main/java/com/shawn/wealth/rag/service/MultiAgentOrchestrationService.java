package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.dto.agent.AgentIntent;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationRequest;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationResponse;
import com.shawn.wealth.rag.dto.tool.CompareResponse;
import com.shawn.wealth.rag.dto.tool.SummarizeResponse;
import com.shawn.wealth.rag.history.AgentHistoryDocument;
import com.shawn.wealth.rag.history.AgentHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class MultiAgentOrchestrationService {

    private final AgentRoutingService routingService;
    private final RagService ragService;
    private final ChatService chatService;
    private final ToolService toolService;
    private final AgentKafkaPublisher kafkaPublisher;
    private final ShortTermMemoryService shortTermMemoryService;
    private final AgentHistoryRepository historyRepository;

    public MultiAgentOrchestrationService(AgentRoutingService routingService,
                                          RagService ragService,
                                          ChatService chatService,
                                          ToolService toolService,
                                          AgentKafkaPublisher kafkaPublisher,
                                          ShortTermMemoryService shortTermMemoryService,
                                          AgentHistoryRepository historyRepository) {
        this.routingService = routingService;
        this.ragService = ragService;
        this.chatService = chatService;
        this.toolService = toolService;
        this.kafkaPublisher = kafkaPublisher;
        this.shortTermMemoryService = shortTermMemoryService;
        this.historyRepository = historyRepository;
    }

    public AgentOrchestrationResponse handle(AgentOrchestrationRequest request) {
        validate(request);

        AgentIntent intent = routingService.route(request);
        shortTermMemoryService.addMessage(request.sessionId(), "USER", request.message());

        return switch (intent) {
            case CHAT -> handleChat(request);
            case SUMMARIZE -> handleSummarize(request);
            case COMPARE -> handleCompare(request);
            case COMPLIANCE_REVIEW -> handleComplianceReview(request);
            case RAG -> handleRag(request);
        };
    }

    private AgentOrchestrationResponse handleRag(AgentOrchestrationRequest request) {
        RagResponse ragResponse = ragService.ask(new RagRequest(request.sessionId(), request.message()));

        shortTermMemoryService.addMessage(request.sessionId(), "ASSISTANT", ragResponse.answer());

        historyRepository.save(new AgentHistoryDocument(
                request.sessionId(),
                null,
                AgentIntent.RAG.name(),
                request.message(),
                ragResponse.answer(),
                ragResponse.status()
        ));

        return new AgentOrchestrationResponse(
                ragResponse.status(),
                null,
                AgentIntent.RAG.name(),
                "RAG",
                ragResponse.answer(),
                ragResponse.sources(),
                ragResponse.message()
        );
    }

    private AgentOrchestrationResponse handleChat(AgentOrchestrationRequest request) {
        String answer = chatService.ask(request.message().replaceFirst("(?i)^chat:", "").trim());

        shortTermMemoryService.addMessage(request.sessionId(), "ASSISTANT", answer);

        historyRepository.save(new AgentHistoryDocument(
                request.sessionId(),
                null,
                AgentIntent.CHAT.name(),
                request.message(),
                answer,
                "SUCCESS"
        ));

        return new AgentOrchestrationResponse(
                "SUCCESS",
                null,
                AgentIntent.CHAT.name(),
                "CHAT",
                answer,
                Collections.emptyList(),
                null
        );
    }

    private AgentOrchestrationResponse handleSummarize(AgentOrchestrationRequest request) {
        String text = request.message().replaceFirst("(?i)^summarize", "").trim();
        SummarizeResponse response = toolService.summarize(text);

        historyRepository.save(new AgentHistoryDocument(
                request.sessionId(),
                null,
                AgentIntent.SUMMARIZE.name(),
                request.message(),
                response.summary(),
                response.status()
        ));

        return new AgentOrchestrationResponse(
                response.status(),
                null,
                AgentIntent.SUMMARIZE.name(),
                "SUMMARIZE",
                response.summary(),
                Collections.emptyList(),
                null
        );
    }

    private AgentOrchestrationResponse handleCompare(AgentOrchestrationRequest request) {
        CompareResponse response = toolService.compare(request.textA(), request.textB());

        historyRepository.save(new AgentHistoryDocument(
                request.sessionId(),
                null,
                AgentIntent.COMPARE.name(),
                request.message(),
                response.comparison(),
                response.status()
        ));

        return new AgentOrchestrationResponse(
                response.status(),
                null,
                AgentIntent.COMPARE.name(),
                "COMPARE",
                response.comparison(),
                Collections.emptyList(),
                null
        );
    }

    private AgentOrchestrationResponse handleComplianceReview(AgentOrchestrationRequest request) {
        String requestId = UUID.randomUUID().toString();

        RagResponse ragResponse = ragService.ask(new RagRequest(request.sessionId(), request.message()));

        kafkaPublisher.publishComplianceRequested(
                requestId,
                request.sessionId(),
                request.customerId(),
                request.message(),
                ragResponse.answer()
        );

        historyRepository.save(new AgentHistoryDocument(
                request.sessionId(),
                requestId,
                AgentIntent.COMPLIANCE_REVIEW.name(),
                request.message(),
                "Compliance review request accepted",
                "ACCEPTED"
        ));

        return new AgentOrchestrationResponse(
                "ACCEPTED",
                requestId,
                AgentIntent.COMPLIANCE_REVIEW.name(),
                "ASYNC_COMPLIANCE_REVIEW",
                "Compliance review request accepted. Use requestId to check the final result.",
                ragResponse.sources(),
                null
        );
    }

    private void validate(AgentOrchestrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("message must not be empty");
        }
    }
}