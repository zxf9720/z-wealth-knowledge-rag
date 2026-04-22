package com.shawn.wealth.rag.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatHistoryService {

    private final Map<String, List<Message>> historyStore = new ConcurrentHashMap<>();

    public List<Message> getHistory(String sessionId) {
        return historyStore.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    public void addUserMessage(String sessionId, String text) {
        getHistory(sessionId).add(new UserMessage(text));
    }

    public void addAssistantMessage(String sessionId, String text) {
        getHistory(sessionId).add(new AssistantMessage(text));
    }

    public void clearHistory(String sessionId) {
        historyStore.remove(sessionId);
    }

    public int size(String sessionId) {
        return getHistory(sessionId).size();
    }
}
