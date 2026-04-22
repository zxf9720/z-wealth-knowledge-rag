package com.shawn.wealth.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class RagBasicTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void testEmbedding() {
        float[] vector = embeddingModel.embed("hello world");
        System.out.println("Vector size = " + vector.length);
    }

    @Test
    void testVectorStore() {
        vectorStore.add(List.of(
                new Document("TFSA is a tax-free savings account in Canada.")
        ));
        System.out.println("Insert success");
    }
}
