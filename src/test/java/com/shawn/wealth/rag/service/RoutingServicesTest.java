package com.shawn.wealth.rag.service;

import com.shawn.wealth.rag.dto.agent.AgentIntent;
import com.shawn.wealth.rag.dto.agent.AgentOrchestrationRequest;
import com.shawn.wealth.rag.dto.agent.AgentRequest;
import com.shawn.wealth.rag.dto.agent.IntentType;
import com.shawn.wealth.rag.dto.agent.ToolType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoutingServicesTest {

    private final IntentRoutingService intentRoutingService = new IntentRoutingService();
    private final ToolSelectionService toolSelectionService = new ToolSelectionService();
    private final AgentRoutingService agentRoutingService = new AgentRoutingService();

    @Test
    void intentRoutingCoversAllAgentRequestBranches() {
        assertThat(intentRoutingService.route(new AgentRequest("s", "summarize this", null, null)))
                .isEqualTo(IntentType.SUMMARIZE);
        assertThat(intentRoutingService.route(new AgentRequest("s", "summary this", null, null)))
                .isEqualTo(IntentType.SUMMARIZE);
        assertThat(intentRoutingService.route(new AgentRequest("s", "compare products", null, null)))
                .isEqualTo(IntentType.COMPARE);
        assertThat(intentRoutingService.route(new AgentRequest("s", "chat: hello", null, null)))
                .isEqualTo(IntentType.CHAT);
        assertThat(intentRoutingService.route(new AgentRequest("s", "What is TFSA?", null, null)))
                .isEqualTo(IntentType.RAG);
        assertThat(intentRoutingService.route(new AgentRequest("s", null, "A", "B")))
                .isEqualTo(IntentType.COMPARE);
    }

    @Test
    void intentRoutingRejectsNullRequest() {
        assertThatThrownBy(() -> intentRoutingService.route(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request must not be null");
    }

    @Test
    void toolSelectionMapsIntentToTool() {
        assertThat(toolSelectionService.select(IntentType.SUMMARIZE)).isEqualTo(ToolType.SUMMARIZE);
        assertThat(toolSelectionService.select(IntentType.COMPARE)).isEqualTo(ToolType.COMPARE);
        assertThat(toolSelectionService.select(IntentType.CHAT)).isEqualTo(ToolType.CHAT);
        assertThat(toolSelectionService.select(IntentType.RAG)).isEqualTo(ToolType.RAG);
    }

    @Test
    void agentRoutingCoversAllOrchestrationBranches() {
        assertThat(agentRoutingService.route(new AgentOrchestrationRequest("s", "needs compliance", "C1", null, null)))
                .isEqualTo(AgentIntent.COMPLIANCE_REVIEW);
        assertThat(agentRoutingService.route(new AgentOrchestrationRequest("s", "summarize this", null, null, null)))
                .isEqualTo(AgentIntent.SUMMARIZE);
        assertThat(agentRoutingService.route(new AgentOrchestrationRequest("s", "compare", null, "A", "B")))
                .isEqualTo(AgentIntent.COMPARE);
        assertThat(agentRoutingService.route(new AgentOrchestrationRequest("s", "chat: hello", null, null, null)))
                .isEqualTo(AgentIntent.CHAT);
        assertThat(agentRoutingService.route(new AgentOrchestrationRequest("s", "What is TFSA?", null, null, null)))
                .isEqualTo(AgentIntent.RAG);
    }
}
