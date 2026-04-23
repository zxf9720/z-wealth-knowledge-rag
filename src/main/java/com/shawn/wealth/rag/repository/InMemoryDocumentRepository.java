package com.shawn.wealth.rag.repository;

import com.shawn.wealth.rag.document.KnowledgeDocument;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryDocumentRepository {

    private final Map<String, KnowledgeDocument> store = new LinkedHashMap<>();

    public void save(KnowledgeDocument document) {
        store.put(document.getId(), document);
    }

    public KnowledgeDocument findById(String id) {
        return store.get(id);
    }

    public List<KnowledgeDocument> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(String id) {
        store.remove(id);
    }

    public boolean existsByFileName(String fileName) {
        return store.values().stream()
                .anyMatch(document -> fileName.equalsIgnoreCase(document.getFileName()));
    }

    public KnowledgeDocument findByFileName(String fileName) {
        return store.values().stream()
                .filter(document -> fileName.equalsIgnoreCase(document.getFileName()))
                .findFirst()
                .orElse(null);
    }
}