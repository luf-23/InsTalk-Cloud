package org.instalk.cloud.instalkaiservice.llm;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAiChatRequestFactory {

    private static final String DEFAULT_MODEL = "qwen-plus";

    public ChatRequest chatRequest(List<ChatMessage> messages, UserAiConfig cfg) {
        return chatRequest(messages, cfg, List.of());
    }

    public ChatRequest chatRequest(List<ChatMessage> messages, UserAiConfig cfg, List<ToolSpecification> tools) {
        OpenAiChatRequestParameters.Builder pb = OpenAiChatRequestParameters.builder()
                .modelName(cfg.getModel() != null ? cfg.getModel() : DEFAULT_MODEL);

        if (tools != null && !tools.isEmpty()) {
            pb.toolSpecifications(tools);
        }

        if (cfg.getTemperature() != null) {
            pb.temperature(cfg.getTemperature().doubleValue());
        }
        if (cfg.getTopP() != null) {
            pb.topP(cfg.getTopP().doubleValue());
        }
        if (cfg.getPresencePenalty() != null) {
            pb.presencePenalty(cfg.getPresencePenalty().doubleValue());
        }
        if (cfg.getMaxTokens() != null) {
            pb.maxOutputTokens(cfg.getMaxTokens());
        }
        if (cfg.getSeed() != null) {
            pb.seed(cfg.getSeed());
        }

        return ChatRequest.builder()
                .messages(messages)
                .parameters(pb.build())
                .build();
    }
}
