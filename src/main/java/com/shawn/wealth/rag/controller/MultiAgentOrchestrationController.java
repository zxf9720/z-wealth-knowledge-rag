package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.agent.AgentOrchestrationRequest;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationResponse;
import com.shawn.wealth.rag.history.AgentHistoryDocument;
import com.shawn.wealth.rag.history.AgentHistoryRepository;
import com.shawn.wealth.rag.service.MultiAgentOrchestrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agent/orchestration")
public class MultiAgentOrchestrationController {

    private final MultiAgentOrchestrationService orchestrationService;
    private final AgentHistoryRepository historyRepository;

    public MultiAgentOrchestrationController(MultiAgentOrchestrationService orchestrationService,
                                             AgentHistoryRepository historyRepository) {
        this.orchestrationService = orchestrationService;
        this.historyRepository = historyRepository;
    }

    @PostMapping("/ask")
    public AgentOrchestrationResponse ask(@RequestBody AgentOrchestrationRequest request) {
        return orchestrationService.handle(request);
    }

    @GetMapping("/history/{sessionId}")
    public List<AgentHistoryDocument> getHistory(@PathVariable String sessionId) {
        return historyRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }

    @GetMapping("/requests/{requestId}")
    public AgentHistoryDocument getByRequestId(@PathVariable String requestId) {
        return historyRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
    }
}