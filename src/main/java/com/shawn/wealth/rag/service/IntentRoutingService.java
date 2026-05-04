package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.agent.AgentRequest;
import com.shawn.wealth.rag.dto.agent.IntentType;
import org.springframework.stereotype.Service;

@Service
public class IntentRoutingService {

    public IntentType route(AgentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (request.textA() != null && !request.textA().isBlank()
                && request.textB() != null && !request.textB().isBlank()) {
            return IntentType.COMPARE;
        }

        String message = request.message() == null ? "" : request.message().trim().toLowerCase();

        if (message.startsWith("summarize") || message.startsWith("summary")) {
            return IntentType.SUMMARIZE;
        }

        if (message.startsWith("compare")) {
            return IntentType.COMPARE;
        }

        if (message.startsWith("chat:")) {
            return IntentType.CHAT;
        }

        return IntentType.RAG;
    }
}
