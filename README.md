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

- Java 25
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Spring AI MCP client (Stateless Streamable HTTP)
- springdoc-openapi 3.0.2
- Ollama / OpenAI
- Qdrant
- Redis
- MongoDB
- Kafka
- Maven
- JaCoCo
- Docker (Eclipse Temurin Java 25 JRE)

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
- MCP server: http://localhost:8084/mcp (stateless Streamable HTTP)

---

## Requirements

- JDK 25
- A stateless MCP server exposing the Streamable HTTP endpoint at `http://localhost:8084/mcp`
- Ollama, Qdrant, Redis, MongoDB, and Kafka using the endpoints listed above

The MCP SDK version is managed by the Spring AI 2.0.0 BOM. The client uses
Streamable HTTP rather than the deprecated SSE transport and is compatible
with a stateless MCP server.

---

## Run

```bash
./mvnw spring-boot:run
```

Build the Java 25 runtime image after packaging the application:

```bash
./mvnw clean package
docker build -t z-wealth-knowledge-rag .
```

When running the container locally, override the configured `localhost`
dependency endpoints with Docker-accessible hostnames or service names.

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
./mvnw verify
```

Report:

```text
target/site/jacoco/index.html
```

---
