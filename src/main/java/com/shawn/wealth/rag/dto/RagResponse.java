package com.shawn.wealth.rag.dto;

import java.util.List;
import java.util.Map;

public record RagResponse(String sessionId,
                          String answer,
                          List<SourceItem> sources) {

    public record SourceItem(
            String text,
            Map<String, Object> metadata
    ) {}
}
