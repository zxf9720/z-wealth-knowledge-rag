package com.shawn.wealth.rag;

import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.rag.dto.SourceItem;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagBasicTest {

    @Test
    void ragRequestStoresSessionAndQuestion() {
        RagRequest request = new RagRequest("session-1", "What is a TFSA?");

        assertThat(request.sessionId()).isEqualTo("session-1");
        assertThat(request.question()).isEqualTo("What is a TFSA?");
    }

    @Test
    void ragResponseStoresAnswerSourcesAndMessage() {
        SourceItem source = new SourceItem("TFSA content", Map.of("fileName", "tfsa.txt"));

        RagResponse response = new RagResponse("SUCCESS", "Hi! answer", List.of(source), null);

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.answer()).isEqualTo("Hi! answer");
        assertThat(response.sources()).containsExactly(source);
        assertThat(response.message()).isNull();
    }
}
