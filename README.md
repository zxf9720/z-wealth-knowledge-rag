# z-wealth-knowledge-rag

## Overview

z-wealth-knowledge-rag is a Spring Boot–based Retrieval-Augmented Generation (RAG) service for wealth management.

It acts as **Agent A (Orchestrator)** in a multi-agent GenAI system and is responsible for:

- RAG-based question answering
- Document ingestion and vector indexing
- Tool routing (summarize, compare)
- Multi-agent orchestration (Agent B & Agent C)
- MCP tool integration
- Async compliance workflows via Kafka

---

## Architecture

```text
                +-----------------------------+
                | z-wealth-knowledge-rag      |
                | (Agent A - Orchestrator)    |
                +-------------+---------------+
                              |
        +---------------------+----------------------+
        |                     |                      |
        v                     v                      v
Customer Service      Compliance Service        MCP Server
(Agent B)             (Agent C)                 (External Tool)
:8082                 :8083                     :8084

External Systems:
- Qdrant (Vector DB)
- Redis (Short-term memory)
- MongoDB (History)
- Kafka (Async events)
- Ollama / OpenAI (LLM)
```

---

## Core Capabilities

- RAG with Qdrant vector search
- Document ingestion (TXT, MD, PDF, DOCX)
- Source-grounded answers
- Tool routing (summarize, compare)
- Multi-agent orchestration
- Kafka-based async compliance flow
- Non-blocking orchestration (WebClient)
- MCP tool integration
- Swagger/OpenAPI

---

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring AI
- Ollama / OpenAI
- Qdrant
- Redis
- MongoDB
- Kafka
- Maven
- JaCoCo

---

## Runtime Defaults

- App: http://localhost:8081
- Ollama: http://localhost:11434
- Qdrant: localhost:6334
- Redis: localhost:6379
- MongoDB: mongodb://localhost:27017/wealth_rag
- Kafka: localhost:9092
- Agent B: http://localhost:8082
- Agent C: http://localhost:8083
- MCP: http://localhost:8084

---

## Run

```bash
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

---

## API Overview

### RAG

```http
POST /rag/ask
```

```json
{
  "sessionId": "s1",
  "question": "What is TFSA?"
}
```

---

### Documents

- POST /documents/upload
- POST /documents/bootstrap
- GET /documents
- DELETE /documents/{id}

---

### Tools

- POST /tools/summarize
- POST /tools/compare

---

### Agent Routing

```http
POST /agent/ask
```

---

### Orchestration

```http
POST /agent/orchestration/ask
GET /agent/orchestration/requests/{requestId}
```

---

### Compliance

- POST /agent/compliance/review
- POST /agent/nonblocking/compliance/review

---

## Multi-Agent Flow

```text
User → Agent A → Kafka → Agent C → Agent B → LLM
```

---

## Design Principles

- Stateless orchestration
- Rule-based decision, LLM explanation
- Kafka decoupling
- MCP external tools
- Grounded RAG (NO_RESULT fallback)

---

## Storage

| Component | Purpose |
|----------|--------|
| Qdrant | vector search |
| Redis | short-term memory |
| MongoDB | history |
| Kafka | async messaging |

---

## Testing

```bash
mvn verify
```

Report:

```text
target/site/jacoco/index.html
```

---
