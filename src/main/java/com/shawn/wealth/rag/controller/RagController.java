package com.shawn.wealth.rag.controller;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.service.ChatHistoryService;
import com.shawn.wealth.rag.service.RagLoadService;
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

    private final RagLoadService ragLoadService;
    private final RagService ragService;
    private final ChatHistoryService chatHistoryService;

    public RagController(RagLoadService ragLoadService, RagService ragService,
                         ChatHistoryService chatHistoryService) {
        this.ragLoadService = ragLoadService;
        this.ragService = ragService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/load")
    public Map<String, String> load() {
        return Map.of("message", ragLoadService.loadSampleDocuments());
    }

//    @PostMapping("/ask")
//    public RagResponse ask(@RequestBody RagRequest request) {
//        String answer = ragService.ask(request.question());
//        return new RagResponse(answer);
//    }

//    @PostMapping("/load-file")
//    public Map<String, String> loadFile() throws Exception {
//        return Map.of("message", ragLoadService.loadFromFile());
//    }

    @PostMapping("/load-file")
    public Map<String, String> loadFile() throws Exception {
        return Map.of("message", ragLoadService.loadFromFile());
    }

    @PostMapping("/load-files")
    public Map<String, String> loadFiles() throws Exception {
        return Map.of("message", ragLoadService.loadAllFiles());
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

    @PostMapping("/load-pdfs")
    public Map<String, String> loadPdfs() throws Exception {
        return Map.of("message", ragLoadService.loadAllPdfFiles());
    }

    @PostMapping("/load-all")
    public Map<String, String> loadAll() throws Exception {
        return Map.of("message", ragLoadService.loadAllSupportedFiles());
    }
}
