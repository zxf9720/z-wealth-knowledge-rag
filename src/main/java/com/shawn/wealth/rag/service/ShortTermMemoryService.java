package com.shawn.wealth.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ShortTermMemoryService {

    private static final int MAX_MESSAGES = 20;
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ShortTermMemoryService(StringRedisTemplate redisTemplate,
                                  ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void addMessage(String sessionId, String role, String content) {
        try {
            String key = key(sessionId);

            String value = objectMapper.writeValueAsString(Map.of(
                    "role", role,
                    "content", content,
                    "timestamp", Instant.now().toString()
            ));

            redisTemplate.opsForList().rightPush(key, value);
            redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
            redisTemplate.expire(key, TTL);

        } catch (Exception e) {
            throw new RuntimeException("Failed to write short-term memory", e);
        }
    }

    public List<String> getRecentMessages(String sessionId) {
        List<String> values = redisTemplate.opsForList().range(key(sessionId), 0, -1);
        return values == null ? List.of() : values;
    }

    private String key(String sessionId) {
        return "agent-a:memory:" + sessionId;
    }
}