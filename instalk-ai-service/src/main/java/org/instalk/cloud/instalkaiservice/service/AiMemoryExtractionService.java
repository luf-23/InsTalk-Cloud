package org.instalk.cloud.instalkaiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.instalkaiservice.llm.UserAiChatRequestFactory;
import org.instalk.cloud.instalkaiservice.mapper.UserAiConfigMapper;
import org.instalk.cloud.common.model.enums.AiMemoryType;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiMemoryExtractionService {

    private static final int MAX_MEMORIES_PER_TURN = 10;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private UserAiChatRequestFactory userAiChatRequestFactory;

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    @Autowired
    private AiContextService aiContextService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calls the chat model to classify and compress salient memories from this turn, then inserts rows.
     */
    public void extractAndPersist(Long userId, Long robotId, String userText, String assistantText) {
        UserAiConfig cfg = userAiConfigMapper.select(robotId);
        if (cfg == null) {
            return;
        }
        String u = userText == null ? "" : userText.trim();
        String a = assistantText == null ? "" : assistantText.trim();
        if (u.isEmpty() && a.isEmpty()) {
            return;
        }

        String rawJson;
        try {
            rawJson = requestMemoryJson(cfg, u, a);
        } catch (Exception e) {
            log.warn("Memory extraction LLM failed robotId={}", robotId, e);
            return;
        }

        for (ExtractedMemoryItem item : parseMemoryItems(rawJson)) {
            aiContextService.insertMemory(userId, robotId, item.type(), item.content());
        }
    }

    private String requestMemoryJson(UserAiConfig cfg, String userText, String assistantText) {
        String prompt = """
                You are a memory curator for a chat assistant. Given the latest user message and assistant reply, output ONLY valid JSON (no markdown code fences) with this exact shape:
                {"memories":[{"type":"FACT|PREFERENCE|EVENT","content":"concise text in Chinese"}]}
                Rules:
                - FACT: stable objective facts about the user or context.
                - PREFERENCE: likes, dislikes, habits, choices.
                - EVENT: notable happenings that may matter later (include time if the user gave it).
                - Compress each content to one short line; merge duplicates; omit greetings and trivialities.
                - At most %d items; use {"memories":[]} if nothing is worth storing.

                User message:
                %s

                Assistant reply:
                %s
                """.formatted(MAX_MEMORIES_PER_TURN, userText, assistantText);

        List<ChatMessage> messages = List.of(UserMessage.from(prompt));
        ChatRequest request = userAiChatRequestFactory.chatRequest(messages, cfg);
        ChatResponse response = chatModel.chat(request);
        if (response == null || response.aiMessage() == null) {
            return "{\"memories\":[]}";
        }
        String text = response.aiMessage().text();
        return text != null && !text.isBlank() ? text.trim() : "{\"memories\":[]}";
    }

    private List<ExtractedMemoryItem> parseMemoryItems(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.startsWith("```")) {
            int start = s.indexOf('{');
            int end = s.lastIndexOf('}');
            if (start >= 0 && end > start) {
                s = s.substring(start, end + 1);
            }
        }
        try {
            JsonNode root = objectMapper.readTree(s);
            JsonNode arr = root.get("memories");
            if (arr == null || !arr.isArray()) {
                return List.of();
            }
            List<ExtractedMemoryItem> out = new ArrayList<>();
            for (JsonNode node : arr) {
                if (out.size() >= MAX_MEMORIES_PER_TURN) {
                    break;
                }
                if (node == null || !node.isObject()) {
                    continue;
                }
                JsonNode typeNode = node.get("type");
                JsonNode contentNode = node.get("content");
                if (contentNode == null || contentNode.isNull()) {
                    continue;
                }
                String content = contentNode.asText("").trim();
                if (content.isEmpty()) {
                    continue;
                }
                AiMemoryType type = typeNode != null && !typeNode.isNull()
                        ? AiMemoryType.fromString(typeNode.asText())
                        : AiMemoryType.FACT;
                out.add(new ExtractedMemoryItem(type, content));
            }
            return out;
        } catch (Exception e) {
            log.debug("Failed to parse memory JSON: {}", e.getMessage());
            return List.of();
        }
    }

    private record ExtractedMemoryItem(AiMemoryType type, String content) {}
}
