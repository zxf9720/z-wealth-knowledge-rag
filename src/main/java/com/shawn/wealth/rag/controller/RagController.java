package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.service.ChatHistoryService;
import com.shawn.wealth.rag.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;

    public RagController(RagService ragService,
                         ChatHistoryService chatHistoryService) {
        this.ragService = ragService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/ask")
    public RagResponse ask(@RequestBody RagRequest request) {
        return ragService.askWithSources(request.sessionId(), request.question());
    }

    @DeleteMapping("/history/{sessionId}")
    public Map<String, String> clearHistory(@PathVariable String sessionId) {
        chatHistoryService.clearHistory(sessionId);
        return Map.of("message", "History cleared for sessionId=" + sessionId);
    }

    @PostMapping("/search")
    public List<String> search(@RequestBody RagRequest request) {
        return ragService.search(request.question())
                .stream()
                .map(Document::getText)
                .toList();
    }

}
