package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.dto.agent.AgentRequest;
import com.shawn.wealth.rag.dto.agent.AgentResponse;
import com.shawn.wealth.rag.dto.agent.IntentType;
import com.shawn.wealth.rag.dto.agent.ToolType;
import com.shawn.wealth.rag.dto.tool.CompareResponse;
import com.shawn.wealth.rag.dto.tool.SummarizeResponse;
import com.shawn.wealth.rag.rag.dto.SourceItem;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AgentService {

    private final IntentRoutingService intentRoutingService;
    private final ToolSelectionService toolSelectionService;
    private final ToolService toolService;
    private final RagService ragService;
    private final ChatService chatService;

    public AgentService(IntentRoutingService intentRoutingService,
                        ToolSelectionService toolSelectionService,
                        ToolService toolService,
                        RagService ragService,
                        ChatService chatService) {
        this.intentRoutingService = intentRoutingService;
        this.toolSelectionService = toolSelectionService;
        this.toolService = toolService;
        this.ragService = ragService;
        this.chatService = chatService;
    }

    public AgentResponse ask(AgentRequest request) {
        validateRequest(request);

        IntentType intent = intentRoutingService.route(request);
        ToolType selectedTool = toolSelectionService.select(intent);

        return switch (selectedTool) {
            case SUMMARIZE -> handleSummarize(request, intent, selectedTool);
            case COMPARE -> handleCompare(request, intent, selectedTool);
            case CHAT -> handleChat(request, intent, selectedTool);
            case RAG -> handleRag(request, intent, selectedTool);
        };
    }

    private AgentResponse handleSummarize(AgentRequest request, IntentType intent, ToolType selectedTool) {
        String text = extractSummarizeText(request.message());

        SummarizeResponse response = toolService.summarize(text);

        return new AgentResponse(
                response.status(),
                intent,
                selectedTool,
                response.summary(),
                Collections.emptyList(),
                null
        );
    }

    private AgentResponse handleCompare(AgentRequest request, IntentType intent, ToolType selectedTool) {
        String textA = request.textA();
        String textB = request.textB();

        if ((textA == null || textA.isBlank()) || (textB == null || textB.isBlank())) {
            throw new IllegalArgumentException("Compare tool requires both textA and textB");
        }

        CompareResponse response = toolService.compare(textA, textB);

        return new AgentResponse(
                response.status(),
                intent,
                selectedTool,
                response.comparison(),
                Collections.emptyList(),
                null
        );
    }

    private AgentResponse handleChat(AgentRequest request, IntentType intent, ToolType selectedTool) {
        String message = stripChatPrefix(request.message());

        String answer = chatService.ask(message);

        return new AgentResponse(
                "SUCCESS",
                intent,
                selectedTool,
                answer,
                Collections.emptyList(),
                null
        );
    }

    private AgentResponse handleRag(AgentRequest request, IntentType intent, ToolType selectedTool) {
        RagResponse ragResponse = ragService.ask(
                new RagRequest(request.sessionId(), request.message())
        );

        List<SourceItem> sources = ragResponse.sources() == null
                ? Collections.emptyList()
                : ragResponse.sources();

        return new AgentResponse(
                ragResponse.status(),
                intent,
                selectedTool,
                ragResponse.answer(),
                sources,
                ragResponse.message()
        );
    }

    private void validateRequest(AgentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (request.sessionId() == null || request.sessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }

        boolean hasMessage = request.message() != null && !request.message().isBlank();
        boolean hasCompareInputs = request.textA() != null && !request.textA().isBlank()
                && request.textB() != null && !request.textB().isBlank();

        if (!hasMessage && !hasCompareInputs) {
            throw new IllegalArgumentException("Either message or compare inputs must be provided");
        }
    }

    private String extractSummarizeText(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty for summarize");
        }

        String trimmed = message.trim();

        if (trimmed.toLowerCase().startsWith("summarize")) {
            String content = trimmed.substring("summarize".length()).trim();
            if (content.isBlank()) {
                throw new IllegalArgumentException("Summarize tool requires text after the command");
            }
            return content;
        }

        if (trimmed.toLowerCase().startsWith("summary")) {
            String content = trimmed.substring("summary".length()).trim();
            if (content.isBlank()) {
                throw new IllegalArgumentException("Summarize tool requires text after the command");
            }
            return content;
        }

        return trimmed;
    }

    private String stripChatPrefix(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Chat message must not be empty");
        }

        String trimmed = message.trim();
        if (trimmed.toLowerCase().startsWith("chat:")) {
            String content = trimmed.substring("chat:".length()).trim();
            if (content.isBlank()) {
                throw new IllegalArgumentException("Chat message must not be empty");
            }
            return content;
        }

        return trimmed;
    }
}