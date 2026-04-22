# z-wealth-knowledge-rag

**Wealth Knowledge RAG (with agentic capabilities)**

A Spring Boot-based Wealth Knowledge RAG (Retrieval-Augmented Generation) system designed for banking and financial scenarios.

This project focuses on retrieving and grounding responses from large-scale policy and compliance documents, such as loan regulations, interest rates, and internal guidelines.

On top of the RAG foundation, the system introduces lightweight agentic capabilities, including tool calling and request routing, to support tasks like summarization, comparison, and draft generation.

---

## Problem

In banking and wealth management scenarios, users need accurate answers based on policy and compliance documents.  
Traditional LLMs may hallucinate without grounding in trusted data, which introduces risks in regulated environments.

---

## Solution

This project implements a RAG-based system that retrieves relevant policy documents and generates grounded answers with citations.

It enhances the core RAG pipeline with agentic capabilities, enabling the system to:
- Select appropriate tools based on user intent
- Route requests dynamically (e.g., retrieval, summarization, comparison)
- Support more flexible and task-oriented interactions

---

## Key Features

- Document ingestion and intelligent chunking
- Embedding and vector-based semantic search
- Grounded answer generation with citations
- Metadata filtering and latest-policy prioritization
- Chat history and conversational context support
- Lightweight agent-style tool calling (summarize / compare / draft)
- Designed for future extension into a multi-agent compliance workflow

---

## Architecture

- **RAG Service**  
  Handles document ingestion, embedding, and retrieval from vector storage

- **LLM Layer**  
  Generates grounded responses with citations based on retrieved context

- **Tool Layer (Agentic Capabilities)**  
  Supports summarization, comparison, and draft generation via tool calling

- **Future Microservices (Planned)**
    - Customer Data Service (Agent B)
    - Compliance Review Service (Agent C)

---

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring AI 1.1.x
- Vector Database (e.g., Redis / Qdrant)
- RESTful APIs

---

## Future Roadmap

- Add Customer Data API Service (Agent B) - z-customer-data-service
- Add Compliance Review Service (Agent C) - z-compliance-review-service
- Introduce policy-to-customer data comparison for compliance checks
- Evolve into a multi-agent compliance workflow

---

## Run

```bash
mvn spring-boot:run



