package org.instalk.cloud.instalkaiconfigservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.instalk.cloud.common.model.dto.AiChatDTO;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AiUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构建AI请求体
     * @param messages 消息历史列表
     * @param userAiConfig 用户AI配置
     * @return 请求体JSON字符串
     */
    public String buildRequestBody(List<AiChatDTO.AiChatMessage> messages, UserAiConfig userAiConfig, String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            // 设置模型
            requestBody.put("model", userAiConfig.getModel() != null ? userAiConfig.getModel() : "qwen-plus");

            // 构建消息数组
            ArrayNode messagesArray = objectMapper.createArrayNode();

            // 添加系统提示词
            if (userAiConfig.getSystemPrompt() != null && !userAiConfig.getSystemPrompt().isEmpty()) {
                ObjectNode systemMessage = objectMapper.createObjectNode();
                systemMessage.put("role", "system");
                systemMessage.put("content", userAiConfig.getSystemPrompt());
                messagesArray.add(systemMessage);
            }

            // 添加历史消息
            if (messages != null && !messages.isEmpty()) {
                for (AiChatDTO.AiChatMessage message : messages) {
                    ObjectNode msgNode = objectMapper.createObjectNode();
                    msgNode.put("role", message.getRole().toLowerCase());
                    msgNode.put("content", message.getContent());
                    messagesArray.add(msgNode);
                }
            }

            // 添加用户当前消息
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messagesArray.add(userMsg);

            requestBody.set("messages", messagesArray);

            // 设置参数
            if (userAiConfig.getTemperature() != null) {
                requestBody.put("temperature", userAiConfig.getTemperature());
            }
            if (userAiConfig.getTopP() != null) {
                requestBody.put("top_p", userAiConfig.getTopP());
            }
            if (userAiConfig.getMaxTokens() != null) {
                requestBody.put("max_tokens", userAiConfig.getMaxTokens());
            }
            if (userAiConfig.getPresencePenalty() != null) {
                requestBody.put("presence_penalty", userAiConfig.getPresencePenalty());
            }
            if (userAiConfig.getSeed() != null) {
                requestBody.put("seed", userAiConfig.getSeed());
            }

            // 开启流式输出
            requestBody.put("stream", true);

            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析流式响应数据
     * 支持模型: deepseek-v3, deepseek-r1, qwq-plus, qwen-max-2025-01-25
     * @param line SSE数据行
     * @return 提取的内容，如果没有内容则返回null
     */
    public String parseStreamResponse(String line) {
        try {
            // 跳过空行
            if (line == null || line.trim().isEmpty()) {
                return null;
            }

            String jsonData = line.trim();

            // 如果有 "data: " 前缀，去除它
            if (jsonData.startsWith("data:")) {
                jsonData = jsonData.substring(5).trim();
            }

            // 检查是否是结束标记
            if ("[DONE]".equals(jsonData)) {
                return null;
            }

            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode choicesNode = rootNode.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode deltaNode = firstChoice.path("delta");

                StringBuilder result = new StringBuilder();

                // 1. 检查思考内容 (reasoning_content) - 用于 deepseek-r1, qwq-plus 等推理模型
                JsonNode reasoningNode = deltaNode.path("reasoning_content");
                if (!reasoningNode.isMissingNode() && reasoningNode.isTextual()) {
                    String reasoning = reasoningNode.asText();
                    if (!reasoning.isEmpty()) {
                        // 可以添加特殊标记区分思考内容和最终答案
                        result.append(reasoning);
                    }
                }

                // 2. 检查普通内容 (content) - 所有模型都支持
                JsonNode contentNode = deltaNode.path("content");
                if (!contentNode.isMissingNode() && contentNode.isTextual()) {
                    String content = contentNode.asText();
                    if (!content.isEmpty()) {
                        result.append(content);
                    }
                }

                // 返回拼接后的内容
                return result.length() > 0 ? result.toString() : null;
            }

            return null;
        } catch (Exception e) {
            // 解析失败时返回null，不中断流
            return null;
        }
    }

    /**
     * 验证用户是否有发送消息的权限
     * @param userAiConfig 用户AI配置
     * @return 是否有权限
     */
    public boolean canSendMessage(UserAiConfig userAiConfig) {
        if (userAiConfig.getDailyMessageLimit() == null) {
            return true; // 无限制
        }

        return userAiConfig.getDailyMessageCount() < userAiConfig.getDailyMessageLimit();
    }


    /**
     * 检查用户是否需要重置对话
     * @param userAiConfig 用户AI配置
     * @return 是否需要重置
     */
    public boolean needsReset(UserAiConfig userAiConfig) {
        // 如果从未重置过，需要重置
        if (userAiConfig.getLastResetDate() == null) {
            return true;
        }
        // 如果最后重置日期不是今天，需要重置
        return !userAiConfig.getLastResetDate().isEqual(LocalDate.now());
    }


    public Long estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) return 0L;

        // 计算中文字符数
        long chineseCharCount = text.chars().filter(ch -> ch >= 0x4e00 && ch <= 0x9fff).count();
        long englishCharCount = text.length() - chineseCharCount;

        // 估算英文部分的tokens：大约4个字符 = 1个token
        long englishTokens = (long) Math.ceil(englishCharCount / 4.0);

        // 中文部分的tokens：每个字符大约1个token
        long chineseTokens = chineseCharCount;

        return englishTokens + chineseTokens;
    }
}
