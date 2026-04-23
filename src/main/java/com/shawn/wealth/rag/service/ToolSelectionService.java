package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.agent.IntentType;
import com.shawn.wealth.rag.dto.agent.ToolType;
import org.springframework.stereotype.Service;

@Service
public class ToolSelectionService {

    public ToolType select(IntentType intent) {
        return switch (intent) {
            case SUMMARIZE -> ToolType.SUMMARIZE;
            case COMPARE -> ToolType.COMPARE;
            case CHAT -> ToolType.CHAT;
            case RAG -> ToolType.RAG;
        };
    }
}
