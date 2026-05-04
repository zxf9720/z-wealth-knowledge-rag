package com.shawn.wealth.rag.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RagLoadServiceTest {

    @Test
    void loadsSampleAndClasspathTextDocuments() throws Exception {
        FakeVectorStore vectorStore = new FakeVectorStore();
        RagLoadService service = new RagLoadService(vectorStore);

        assertThat(service.loadSampleDocuments()).isEqualTo("Loaded 3 documents into Qdrant.");
        assertThat(service.loadFromFile()).startsWith("Loaded ").endsWith(" clean chunks.");
        assertThat(service.loadAllTxtFiles()).contains("chunks from").contains("files");

        assertThat(vectorStore.addedBatches).hasSize(3);
        assertThat(vectorStore.addedBatches.get(0)).hasSize(3);
        assertThat(vectorStore.addedBatches.get(1)).isNotEmpty();
        assertThat(vectorStore.addedBatches.get(2)).isNotEmpty();
    }

    @Test
    void loadsAllSupportedClasspathDocuments() throws Exception {
        FakeVectorStore vectorStore = new FakeVectorStore();
        RagLoadService service = new RagLoadService(vectorStore);

        String result = service.loadAllSupportedFiles();

        assertThat(result).contains("chunks from").contains("txt files").contains("pdf files");
        assertThat(vectorStore.addedBatches).hasSize(1);
        assertThat(vectorStore.addedBatches.get(0)).isNotEmpty();
    }

    private static class FakeVectorStore implements VectorStore {
        final List<List<Document>> addedBatches = new ArrayList<>();

        @Override
        public void add(List<Document> documents) {
            addedBatches.add(new ArrayList<>(documents));
        }

        @Override
        public void delete(List<String> idList) {
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return List.of();
        }

        @Override
        public <T> Optional<T> getNativeClient() {
            return Optional.empty();
        }
    }
}
