package com.shawn.wealth.rag.dto;

/**
 * Request object for RAG (Retrieval-Augmented Generation) operations.
 *
 * The sessionId represents a unique conversation context and is used to isolate ChatMemory
 * across different users or sessions. This ensures that each conversation maintains its own
 * context and prevents message leakage between users.
 *
 * The question field contains the user query that will be processed by the RAG pipeline,
 * including retrieval from the vector store and response generation by the LLM.
 */
public record RagRequest(String sessionId, String question) {
}
