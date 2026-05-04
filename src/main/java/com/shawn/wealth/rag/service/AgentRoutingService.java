package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.agent.AgentIntent;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationRequest;
import org.springframework.stereotype.Service;

@Service
public class AgentRoutingService {

    public AgentIntent route(AgentOrchestrationRequest request) {
        String message = request.message() == null ? "" : request.message().toLowerCase();

        if (request.customerId() != null && !request.customerId().isBlank()
                && message.contains("compliance")) {
            return AgentIntent.COMPLIANCE_REVIEW;
        }

        if (message.startsWith("summarize")) {
            return AgentIntent.SUMMARIZE;
        }

        if (request.textA() != null && request.textB() != null) {
            return AgentIntent.COMPARE;
        }

        if (message.startsWith("chat:")) {
            return AgentIntent.CHAT;
        }

        return AgentIntent.RAG;
    }
}