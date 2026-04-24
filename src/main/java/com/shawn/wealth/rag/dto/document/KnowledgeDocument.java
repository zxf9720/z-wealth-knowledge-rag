package com.shawn.wealth.rag.dto.document;

import java.time.LocalDateTime;
import java.util.Map;

public class KnowledgeDocument {

    private String id;
    private String fileName;
    private String contentType;
    private String rawText;
    private Map<String, Object> metadata;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeDocument() {
    }

    public KnowledgeDocument(String id, String fileName, String contentType, String rawText,
                             Map<String, Object> metadata, String status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.rawText = rawText;
        this.metadata = metadata;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}