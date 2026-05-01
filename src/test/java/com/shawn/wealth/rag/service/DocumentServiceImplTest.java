package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.document.DocumentResponse;
import com.shawn.wealth.rag.dto.document.IngestResponse;
import com.shawn.wealth.rag.dto.document.KnowledgeDocument;
import com.shawn.wealth.rag.dto.document.TextIngestRequest;
import com.shawn.wealth.rag.repository.InMemoryDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentServiceImplTest {

    private FakeVectorStore vectorStore;
    private InMemoryDocumentRepository repository;
    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        vectorStore = new FakeVectorStore();
        repository = new InMemoryDocumentRepository();
        service = new DocumentServiceImpl(vectorStore, repository);
    }

    @Test
    void ingestTextIndexesChunksAndStoresMetadata() {
        String text = "x".repeat(501);
        IngestResponse response = service.ingestText(new TextIngestRequest(
                text,
                "tfsa.txt",
                Map.of("category", "banking")
        ));

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.chunks()).isEqualTo(2);

        KnowledgeDocument stored = repository.findById(response.documentId());
        assertThat(stored.getFileName()).isEqualTo("tfsa.txt");
        assertThat(stored.getContentType()).isEqualTo("text/plain");
        assertThat(stored.getStatus()).isEqualTo("INDEXED");
        assertThat(stored.getMetadata()).containsEntry("category", "banking");

        assertThat(vectorStore.addedBatches).hasSize(1);
        assertThat(vectorStore.addedBatches.get(0)).hasSize(2);
        assertThat(vectorStore.addedBatches.get(0).get(0).getMetadata()).containsEntry("chunkStart", 0);
        assertThat(vectorStore.addedBatches.get(0).get(1).getMetadata()).containsEntry("chunkStart", 500);
    }

    @Test
    void ingestTextUsesDefaultFileNameAndReplacesExistingDocument() {
        IngestResponse first = service.ingestText(new TextIngestRequest("old", null, null));
        IngestResponse second = service.ingestText(new TextIngestRequest("new", null, null));

        assertThat(repository.findById(first.documentId())).isNull();
        assertThat(repository.findById(second.documentId()).getRawText()).isEqualTo("new");
        assertThat(vectorStore.deletedFilters).contains("documentId == '" + first.documentId() + "'");
        assertThat(vectorStore.addedBatches).hasSize(2);
    }

    @Test
    void uploadRejectsMissingAndUnsupportedFiles() {
        assertThatThrownBy(() -> service.upload(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No files were provided");

        MockMultipartFile unsupported = new MockMultipartFile(
                "files",
                "data.exe",
                "application/octet-stream",
                "data".getBytes()
        );

        assertThatThrownBy(() -> service.upload(List.of(unsupported)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported file type: data.exe");
    }

    @Test
    void uploadTextFileIndexesDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "guide.md",
                "text/markdown",
                "TFSA guide".getBytes()
        );

        List<IngestResponse> responses = service.upload(List.of(file));

        assertThat(responses).hasSize(1);
        assertThat(repository.findById(responses.get(0).documentId()).getFileName()).isEqualTo("guide.md");
    }

    @Test
    void listDeleteAndReindexDocuments() {
        IngestResponse ingested = service.ingestText(new TextIngestRequest("content", "loans.txt", null));

        List<DocumentResponse> documents = service.listDocuments();
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).id()).isEqualTo(ingested.documentId());

        IngestResponse reindexed = service.reindexDocument(ingested.documentId());
        assertThat(reindexed.status()).isEqualTo("REINDEXED");
        assertThat(repository.findById(ingested.documentId()).getStatus()).isEqualTo("REINDEXED");
        assertThat(vectorStore.deletedFilters).contains("documentId == '" + ingested.documentId() + "'");

        service.deleteDocument(ingested.documentId());
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void deleteAndReindexRejectUnknownDocument() {
        assertThatThrownBy(() -> service.deleteDocument("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document not found: missing");
        assertThatThrownBy(() -> service.reindexDocument("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document not found: missing");
    }

    @Test
    void bootstrapIndexesSupportedLocalDocuments() {
        KnowledgeDocument existing = new KnowledgeDocument(
                "existing-id",
                "banking.txt",
                "text/plain",
                "old",
                Map.of("documentId", "existing-id"),
                "INDEXED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        repository.save(existing);

        var response = service.bootstrapDocuments();

        assertThat(response.indexed()).isGreaterThan(0);
        assertThat(response.replaced()).isEqualTo(1);
        assertThat(repository.findByFileName("banking.txt").getId()).isNotEqualTo("existing-id");
    }

    private static class FakeVectorStore implements VectorStore {
        private final List<List<Document>> addedBatches = new ArrayList<>();
        private final List<String> deletedFilters = new ArrayList<>();

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
        public void delete(String filterExpression) {
            deletedFilters.add(filterExpression);
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
