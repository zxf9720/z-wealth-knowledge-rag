# z-wealth-knowledge-rag

## Wealth Knowledge RAG System

A production-style Retrieval-Augmented Generation (RAG) system built with Spring Boot, Spring AI, a vector database (Qdrant), and LLM integration.

This project is designed for banking and wealth management scenarios, focusing on accuracy, compliance, and explainability.

---

## Overview

In financial systems, users require reliable answers based on policy and compliance documents such as loan regulations, interest rates, and internal guidelines.

Traditional LLMs may produce hallucinated responses, which introduces risk in regulated environments.

This system addresses the problem by combining:

Retrieval (Vector Database) + Generation (LLM)

It retrieves relevant knowledge from a controlled data source and generates grounded answers with traceable sources.

In addition, the system includes lightweight tool capabilities such as summarization and comparison, enabling more task-oriented interactions beyond simple question answering.


Core capabilities include:

- Document ingestion and indexing
- Vector-based retrieval
- Grounded answer generation
- Session-based conversational memory

---

## Problem

In banking environments:

- Data must be accurate and auditable
- Responses must be traceable
- Hallucination must be minimized

Using LLM alone:

- Not grounded
- Not auditable
- High compliance risk

---

## Solution

This project implements a RAG-based architecture:

User Question  
→ Retrieve relevant documents (Vector DB)  
→ Build grounded context  
→ Generate answer (LLM)  
→ Return answer with sources

Key improvements:

- Grounded responses instead of pure generation
- Source attribution for auditability
- Structured response handling (SUCCESS / NO_RESULT)

If no relevant data is found, the system returns a controlled NO_RESULT response instead of allowing the model to hallucinate.

---

## Key Features

### Document Ingestion

- Supports TXT / MD / PDF / DOCX
- Automatic chunking (approx. 500 characters)
- Metadata enrichment (documentId, fileName, chunk range)
- Local document bootstrap
- Full lifecycle management:
  - Upload
  - List
  - Delete (including vector cleanup)
  - Reindex

---

### Retrieval and RAG

- Vector-based semantic search
- Top-K retrieval
- Similarity threshold filtering
- Metadata-based filtering
- Prompt grounding with retrieved context
- Source-aware responses

---

### Conversational Support

- Session-based chat memory
- Window-limited context (~20 messages)
- Supports follow-up questions

---

### Lightweight Tool Capabilities

- Summarize long policy or knowledge content into concise responses
- Compare two text inputs to highlight key differences and similarities
- Designed as task-oriented endpoints, which can evolve into internal agent tools

---

### API Layer

- RESTful APIs
- OpenAPI (Swagger) support
- Postman-ready testing
- Structured responses:
  - SUCCESS
  - NO_RESULT
  - ERROR

---

## Architecture

### High-Level Flow

User  
→ Controller Layer  
→ Service Layer  
→ Retrieval Layer  
→ Vector Database  
→ LLM

---

### Detailed Architecture

Client / Postman  
→ Controllers
- ChatController
- DocumentController
- RagController
- ToolController

→ Services
- ChatService
- DocumentService
- RagService
- ToolService

→ Core Components
- ChatClient (Spring AI)
- ChatMemory (session-based)
- VectorStore

→ Storage
- Vector Database (Qdrant)
- InMemoryDocumentRepository

---

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring AI 1.1.x
- Qdrant (Vector Database)
- REST APIs
- Swagger / OpenAPI

---

## Future Roadmap

- Customer Data API Service (Agent B) - z-customer-data-service
- Compliance Review Service (Agent C) - z-compliance-review-service
- Introduce policy-to-customer data comparison for compliance checks
- Evolve into a multi-agent compliance workflow
- Kafka-based asynchronous ingestion
- Redis caching for low latency
- Hybrid search and reranking
- Persistent document storage

---

## Run

```bash
mvn spring-boot:run
```