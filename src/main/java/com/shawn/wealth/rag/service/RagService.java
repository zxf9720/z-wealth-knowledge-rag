package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagService {

    private final ChatClient chatClient;

    // not advanced one:
//    chatModel.call(message);
    private final VectorStore vectorStore;

    private final ChatHistoryService chatHistoryService;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                      ChatHistoryService chatHistoryService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.chatHistoryService = chatHistoryService;
    }


    public String ask(String question) {
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(5)
                        .similarityThreshold(0.0d)
                        .build())
                .build();

        return chatClient.prompt()
                .system("You are a banking assistant. Answer briefly and professionally. " +
                        "If the answer is not in the retrieved context, say you don't know.")
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }

    public List<Document> search(String question) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .similarityThreshold(0.0d)
                        .build()
        );
    }

//    public RagResponse askWithSources(String question) {
//
//        List<Document> docs = vectorStore.similaritySearch(
//                SearchRequest.builder()
//                        .query(question)
//                        .topK(5)
//                        .similarityThreshold(0.0)
//                        .build()
//        );
//        System.out.println("Before dedup: " + docs.size());
//        docs = deduplicateAndSort(docs);
//        System.out.println("After dedup: " + docs.size());
//
//        docs = docs.stream().limit(3).toList();
//
//        String context = docs.stream()
//                .map(Document::getText)
//                .reduce("", (a, b) -> a + "\n\n" + b);
//
//        String answer = chatClient.prompt()
//                .system("""
//                        You are a banking assistant.
//                        Answer only based on the provided context.
//                        If the answer is not in the context, say "I don't know."
//                        """)
//                .user("""
//                        Context:
//                        %s
//
//                        Question:
//                        %s
//                        """.formatted(context, question))
//                .call()
//                .content();
//
//        List<RagResponse.SourceItem> sources = docs.stream()
//                .map(doc -> new RagResponse.SourceItem(
//                        doc.getText(),
//                        doc.getMetadata()
//                ))
//                .toList();
//
//        return new RagResponse(answer, sources);
//    }

//    private List<Document> deduplicateAndSort(List<Document> docs) {
//
//        Map<String, Document> uniqueMap = new LinkedHashMap<>();
//
//        for (Document doc : docs) {
//            String text = doc.getText();
//
//            if (!uniqueMap.containsKey(text)) {
//                uniqueMap.put(text, doc);
//            } else {
//                Document existing = uniqueMap.get(text);
//
//                double existingDistance = ((Number) existing.getMetadata()
//                        .getOrDefault("distance", 1.0)).doubleValue();
//
//                double currentDistance = ((Number) doc.getMetadata()
//                        .getOrDefault("distance", 1.0)).doubleValue();
//
//                if (currentDistance < existingDistance) {
//                    uniqueMap.put(text, doc);
//                }
//            }
//        }
//
//        List<Document> uniqueDocs = new ArrayList<>(uniqueMap.values());
//
//        uniqueDocs.sort(Comparator.comparingDouble(doc ->
//                ((Number) doc.getMetadata().getOrDefault("distance", 1.0)).doubleValue()
//        ));
//
//        return uniqueDocs;
//    }

    public RagResponse askWithSources(String sessionId, String question) {

        // 1) 先做检索
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .similarityThreshold(0.0)
                        .build()
        );

        docs = deduplicateAndSort(docs);
        docs = docs.stream().limit(3).toList();

        // 2) 拼 RAG context
        String context = docs.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n\n" + b)
                .trim();

        // 3) 取历史消息
        List<Message> history = new ArrayList<>(chatHistoryService.getHistory(sessionId));

        // 可选：防止历史无限增长，只保留最近 10 条
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }

        // 4) 用 history + context + current question 调 LLM
        String answer = chatClient.prompt()
                .system("""
                        You are a banking assistant.
                        Answer briefly and professionally.
                        Use the retrieved context as the primary source of truth.
                        If the answer is not in the context, say "I don't know."
                        """)
                .messages(history)
                .user("""
                        Retrieved context:
                        %s

                        Current user question:
                        %s
                        """.formatted(context, question))
                .call()
                .content();

        // 5) 把当前轮次存入 history
        chatHistoryService.addUserMessage(sessionId, question);
        chatHistoryService.addAssistantMessage(sessionId, answer);

        // 6) 返回 sources
        List<RagResponse.SourceItem> sources = docs.stream()
                .map(doc -> new RagResponse.SourceItem(
                        doc.getText(),
                        doc.getMetadata()
                ))
                .toList();

        return new RagResponse(sessionId, answer, sources);
    }

    private List<Document> deduplicateAndSort(List<Document> docs) {
        Map<String, Document> uniqueMap = new LinkedHashMap<>();

        for (Document doc : docs) {
            String text = doc.getText();

            if (!uniqueMap.containsKey(text)) {
                uniqueMap.put(text, doc);
            } else {
                Document existing = uniqueMap.get(text);

                double existingDistance = ((Number) existing.getMetadata()
                        .getOrDefault("distance", 1.0)).doubleValue();

                double currentDistance = ((Number) doc.getMetadata()
                        .getOrDefault("distance", 1.0)).doubleValue();

                if (currentDistance < existingDistance) {
                    uniqueMap.put(text, doc);
                }
            }
        }

        List<Document> uniqueDocs = new ArrayList<>(uniqueMap.values());

        uniqueDocs.sort(Comparator.comparingDouble(doc ->
                ((Number) doc.getMetadata().getOrDefault("distance", 1.0)).doubleValue()
        ));

        return uniqueDocs;
    }

}