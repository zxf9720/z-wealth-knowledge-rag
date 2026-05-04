package com.shawn.wealth.rag.dto.agent;

import com.shawn.wealth.rag.rag.dto.SourceItem;

import java.util.List;

public record AgentOrchestrationResponse(
        String status,
        String requestId,
        String intent,
        String selectedTool,
        String answer,
        List<SourceItem> sources,
        String message
) {
}