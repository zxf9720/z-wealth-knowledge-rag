package com.shawn.wealth.rag.repository;

import com.shawn.wealth.rag.dto.document.KnowledgeDocument;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDocumentRepositoryTest {

    private final InMemoryDocumentRepository repository = new InMemoryDocumentRepository();

    @Test
    void savesFindsListsAndDeletesDocuments() {
        KnowledgeDocument document = document("id-1", "TFSA.TXT");

        repository.save(document);

        assertThat(repository.findById("id-1")).isSameAs(document);
        assertThat(repository.findAll()).containsExactly(document);
        assertThat(repository.existsByFileName("tfsa.txt")).isTrue();
        assertThat(repository.findByFileName("tfsa.txt")).isSameAs(document);

        repository.delete("id-1");

        assertThat(repository.findById("id-1")).isNull();
        assertThat(repository.findAll()).isEmpty();
        assertThat(repository.existsByFileName("tfsa.txt")).isFalse();
        assertThat(repository.findByFileName("tfsa.txt")).isNull();
    }

    private KnowledgeDocument document(String id, String fileName) {
        return new KnowledgeDocument(
                id,
                fileName,
                "text/plain",
                "content",
                Map.of("documentId", id),
                "INDEXED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
