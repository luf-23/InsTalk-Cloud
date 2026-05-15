package org.instalk.cloud.instalkaiservice.llm;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserAiChatRequestFactory {

    /** 与前端 RobotConfigDialog 默认选项一致 */
    private static final String DEFAULT_MODEL = "deepseek-v3";

    /** 与前端 RobotConfigDialog.modelOptions 的 value 一致 */
    private static final Set<String> ALLOWED_MODELS = Set.of(
            "deepseek-v3",
            "deepseek-r1",
            "qwq-plus",
            "qwen-max-2025-01-25");

    public ChatRequest chatRequest(List<ChatMessage> messages, UserAiConfig cfg) {
        return chatRequest(messages, cfg, List.of());
    }

    public ChatRequest chatRequest(List<ChatMessage> messages, UserAiConfig cfg, List<ToolSpecification> tools) {
        OpenAiChatRequestParameters.Builder pb = OpenAiChatRequestParameters.builder()
                .modelName(resolveModel(cfg));

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

    private static String resolveModel(UserAiConfig cfg) {
        String m = cfg.getModel();
        if (m != null && ALLOWED_MODELS.contains(m)) {
            return m;
        }
        return DEFAULT_MODEL;
    }
}
