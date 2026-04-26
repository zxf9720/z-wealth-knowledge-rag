package com.shawn.wealth.rag.history;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AgentHistoryRepository extends MongoRepository<AgentHistoryDocument, String> {

    List<AgentHistoryDocument> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    Optional<AgentHistoryDocument> findByRequestId(String requestId);
}