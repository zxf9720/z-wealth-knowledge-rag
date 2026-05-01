package com.shawn.wealth.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.wealth.rag.dto.RagRequest;
import com.shawn.wealth.rag.dto.RagResponse;
import com.shawn.wealth.rag.dto.agent.AgentIntent;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationRequest;
import com.shawn.wealth.rag.dto.agent.AgentRequest;
import com.shawn.wealth.rag.dto.agent.IntentType;
import com.shawn.wealth.rag.dto.agent.ToolType;
import com.shawn.wealth.rag.dto.tool.CompareResponse;
import com.shawn.wealth.rag.dto.tool.SummarizeResponse;
import com.shawn.wealth.rag.history.AgentHistoryDocument;
import com.shawn.wealth.rag.history.AgentHistoryRepository;
import com.shawn.wealth.rag.rag.dto.SourceItem;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.Resource;

import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentAndOrchestrationServiceTest {

    @Test
    void agentServiceHandlesAllToolBranches() {
        AgentService service = new AgentService(
                new IntentRoutingService(),
                new ToolSelectionService(),
                new FakeToolService(),
                new FakeRagService(),
                new FakeChatService()
        );

        var summary = service.ask(new AgentRequest("s", "summarize long text", null, null));
        assertThat(summary.status()).isEqualTo("SUCCESS");
        assertThat(summary.intent()).isEqualTo(IntentType.SUMMARIZE);
        assertThat(summary.selectedTool()).isEqualTo(ToolType.SUMMARIZE);
        assertThat(summary.answer()).isEqualTo("summary:long text");

        var comparison = service.ask(new AgentRequest("s", null, "A", "B"));
        assertThat(comparison.intent()).isEqualTo(IntentType.COMPARE);
        assertThat(comparison.answer()).isEqualTo("compare:A|B");

        var chat = service.ask(new AgentRequest("s", "chat: hello", null, null));
        assertThat(chat.selectedTool()).isEqualTo(ToolType.CHAT);
        assertThat(chat.answer()).isEqualTo("chat:hello");

        var rag = service.ask(new AgentRequest("s", "What is TFSA?", null, null));
        assertThat(rag.selectedTool()).isEqualTo(ToolType.RAG);
        assertThat(rag.sources()).hasSize(1);
        assertThat(rag.message()).isNull();
    }

    @Test
    void agentServiceValidatesInputs() {
        AgentService service = new AgentService(
                new IntentRoutingService(),
                new ToolSelectionService(),
                new FakeToolService(),
                new FakeRagService(),
                new FakeChatService()
        );

        assertThatThrownBy(() -> service.ask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request must not be null");
        assertThatThrownBy(() -> service.ask(new AgentRequest(" ", "hello", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sessionId must not be empty");
        assertThatThrownBy(() -> service.ask(new AgentRequest("s", null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either message or compare inputs must be provided");
        assertThatThrownBy(() -> service.ask(new AgentRequest("s", "summarize", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Summarize tool requires text after the command");
        assertThatThrownBy(() -> service.ask(new AgentRequest("s", "chat:", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chat message must not be empty");
    }

    @Test
    void orchestrationServiceHandlesAllRoutesAndPersistsHistory() {
        FakeKafkaPublisher publisher = new FakeKafkaPublisher();
        FakeShortTermMemoryService memory = new FakeShortTermMemoryService();
        FakeHistoryRepository history = new FakeHistoryRepository();
        MultiAgentOrchestrationService service = new MultiAgentOrchestrationService(
                new AgentRoutingService(),
                new FakeRagService(),
                new FakeChatService(),
                new FakeToolService(),
                publisher,
                memory,
                history.proxy()
        );

        var chat = service.handle(new AgentOrchestrationRequest("s", "chat: hi", null, null, null));
        assertThat(chat.intent()).isEqualTo(AgentIntent.CHAT.name());
        assertThat(chat.answer()).isEqualTo("chat:hi");

        var summary = service.handle(new AgentOrchestrationRequest("s", "summarize text", null, null, null));
        assertThat(summary.selectedTool()).isEqualTo("SUMMARIZE");
        assertThat(summary.answer()).isEqualTo("summary:text");

        var compare = service.handle(new AgentOrchestrationRequest("s", "compare", null, "A", "B"));
        assertThat(compare.selectedTool()).isEqualTo("COMPARE");
        assertThat(compare.answer()).isEqualTo("compare:A|B");

        var rag = service.handle(new AgentOrchestrationRequest("s", "What is TFSA?", null, null, null));
        assertThat(rag.selectedTool()).isEqualTo("RAG");
        assertThat(rag.sources()).hasSize(1);

        var compliance = service.handle(new AgentOrchestrationRequest("s", "needs compliance", "C1", null, null));
        assertThat(compliance.status()).isEqualTo("ACCEPTED");
        assertThat(compliance.requestId()).isNotBlank();
        assertThat(publisher.publishedRequestId).isEqualTo(compliance.requestId());

        assertThat(memory.messages).hasSize(7);
        assertThat(history.saved).hasSize(5);
    }

    @Test
    void orchestrationServiceValidatesInputs() {
        MultiAgentOrchestrationService service = new MultiAgentOrchestrationService(
                new AgentRoutingService(),
                new FakeRagService(),
                new FakeChatService(),
                new FakeToolService(),
                new FakeKafkaPublisher(),
                new FakeShortTermMemoryService(),
                new FakeHistoryRepository().proxy()
        );

        assertThatThrownBy(() -> service.handle(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request must not be null");
        assertThatThrownBy(() -> service.handle(new AgentOrchestrationRequest("", "message", null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sessionId must not be empty");
        assertThatThrownBy(() -> service.handle(new AgentOrchestrationRequest("s", " ", null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("message must not be empty");
    }

    private static class FakeToolService extends ToolService {
        FakeToolService() {
            super(new NoopChatClientBuilder());
        }

        @Override
        public SummarizeResponse summarize(String text) {
            return new SummarizeResponse("SUCCESS", "summary:" + text);
        }

        @Override
        public CompareResponse compare(String textA, String textB) {
            return new CompareResponse("SUCCESS", "compare:" + textA + "|" + textB);
        }
    }

    private static class FakeChatService extends ChatService {
        FakeChatService() {
            super(new NoopChatClientBuilder());
        }

        @Override
        public String ask(String message) {
            return "chat:" + message;
        }
    }

    private static class FakeRagService extends RagService {
        FakeRagService() {
            super(new NoopChatClientBuilder(), new NoopVectorStore(), (ChatMemory) null);
        }

        @Override
        public RagResponse ask(RagRequest request) {
            return new RagResponse(
                    "SUCCESS",
                    "rag:" + request.question(),
                    List.of(new SourceItem("source", Map.of("sessionId", request.sessionId()))),
                    null
            );
        }
    }

    private static class FakeKafkaPublisher extends AgentKafkaPublisher {
        String publishedRequestId;

        FakeKafkaPublisher() {
            super(null, new ObjectMapper(), "topic");
        }

        @Override
        public void publishComplianceRequested(String requestId, String sessionId, String customerId, String question, String policyAnswer) {
            this.publishedRequestId = requestId;
        }
    }

    private static class FakeShortTermMemoryService extends ShortTermMemoryService {
        final List<String> messages = new ArrayList<>();

        FakeShortTermMemoryService() {
            super(null, new ObjectMapper());
        }

        @Override
        public void addMessage(String sessionId, String role, String content) {
            messages.add(sessionId + ":" + role + ":" + content);
        }
    }

    private static class FakeHistoryRepository {
        final List<AgentHistoryDocument> saved = new ArrayList<>();

        AgentHistoryRepository proxy() {
            return (AgentHistoryRepository) Proxy.newProxyInstance(
                    AgentHistoryRepository.class.getClassLoader(),
                    new Class<?>[]{AgentHistoryRepository.class},
                    (proxy, method, args) -> {
                        if ("save".equals(method.getName())) {
                            AgentHistoryDocument document = (AgentHistoryDocument) args[0];
                            saved.add(document);
                            return document;
                        }
                        if ("findBySessionIdOrderByCreatedAtDesc".equals(method.getName())) {
                            return saved;
                        }
                        if ("findByRequestId".equals(method.getName())) {
                            return saved.stream()
                                    .filter(document -> args[0].equals(document.getRequestId()))
                                    .findFirst();
                        }
                        if ("toString".equals(method.getName())) {
                            return "FakeHistoryRepository";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                        return null;
                    }
            );
        }
    }

    private static class NoopVectorStore implements VectorStore {
        @Override
        public void add(List<Document> documents) {
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

    private static class NoopChatClientBuilder implements ChatClient.Builder {
        @Override public ChatClient.Builder defaultAdvisors(Advisor... advisors) { return this; }
        @Override public ChatClient.Builder defaultAdvisors(Consumer<ChatClient.AdvisorSpec> consumer) { return this; }
        @Override public ChatClient.Builder defaultAdvisors(List<Advisor> advisors) { return this; }
        @Override public ChatClient.Builder defaultOptions(ChatOptions chatOptions) { return this; }
        @Override public ChatClient.Builder defaultUser(String text) { return this; }
        @Override public ChatClient.Builder defaultUser(Resource text, Charset charset) { return this; }
        @Override public ChatClient.Builder defaultUser(Resource text) { return this; }
        @Override public ChatClient.Builder defaultUser(Consumer<ChatClient.PromptUserSpec> consumer) { return this; }
        @Override public ChatClient.Builder defaultSystem(String text) { return this; }
        @Override public ChatClient.Builder defaultSystem(Resource text, Charset charset) { return this; }
        @Override public ChatClient.Builder defaultSystem(Resource text) { return this; }
        @Override public ChatClient.Builder defaultSystem(Consumer<ChatClient.PromptSystemSpec> consumer) { return this; }
        @Override public ChatClient.Builder defaultTemplateRenderer(TemplateRenderer templateRenderer) { return this; }
        @Override public ChatClient.Builder defaultToolNames(String... toolNames) { return this; }
        @Override public ChatClient.Builder defaultTools(Object... tools) { return this; }
        @Override public ChatClient.Builder defaultToolCallbacks(ToolCallback... toolCallbacks) { return this; }
        @Override public ChatClient.Builder defaultToolCallbacks(List<ToolCallback> toolCallbacks) { return this; }
        @Override public ChatClient.Builder defaultToolCallbacks(ToolCallbackProvider... toolCallbackProviders) { return this; }
        @Override public ChatClient.Builder defaultToolContext(Map<String, Object> toolContext) { return this; }
        @Override public ChatClient.Builder clone() { return this; }
        @Override public ChatClient build() { return null; }
    }
}
