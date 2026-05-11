package org.instalk.cloud.instalkaiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instalk.cloud.instalkaiservice.llm.EmbeddingVectorClient;
import org.instalk.cloud.instalkaiservice.mapper.AiChatSummaryMapper;
import org.instalk.cloud.instalkaiservice.mapper.AiMemoryMapper;
import org.instalk.cloud.common.model.dto.AiChatDTO;
import org.instalk.cloud.common.model.enums.AiMemoryType;
import org.instalk.cloud.common.model.po.AiChatSummary;
import org.instalk.cloud.common.model.po.AiMemory;
import org.instalk.cloud.common.model.po.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiContextService {

    public static final int DEFAULT_WINDOW_SIZE = 12;
    public static final int DEFAULT_SUMMARY_TRIGGER_SIZE = 24;
    public static final int DEFAULT_RAG_TOP_K = 6;
    public static final int DEFAULT_MAX_MEMORY_ITEMS = 200;

    @Autowired
    private AiChatSummaryMapper aiChatSummaryMapper;

    @Autowired
    private AiMemoryMapper aiMemoryMapper;

    @Autowired
    private AiUsagePolicy aiUsagePolicy;

    @Autowired
    private EmbeddingVectorClient embeddingVectorClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds LLM-visible context: optional rolling summary + recent message window.
     * Memory retrieval is delegated to the {@code search_memories} tool during chat.
     */
    public List<AiChatDTO.AiChatMessage> buildContext(Long userId,
                                                      Long robotId,
                                                      List<Message> historyMessages,
                                                      int windowSize,
                                                      boolean includeSummary) {
        List<AiChatDTO.AiChatMessage> result = new ArrayList<>();

        if (includeSummary) {
            AiChatSummary summary = aiChatSummaryMapper.selectByUserAndRobot(userId, robotId);
            if (summary != null && summary.getSummary() != null && !summary.getSummary().isBlank()) {
                AiChatDTO.AiChatMessage summaryMessage = new AiChatDTO.AiChatMessage();
                summaryMessage.setRole("system");
                summaryMessage.setContent("对话摘要:\n" + summary.getSummary());
                result.add(summaryMessage);
            }
        }

        if (historyMessages == null || historyMessages.isEmpty()) {
            return result;
        }

        List<Message> sorted = historyMessages.stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .toList();
        int start = Math.max(sorted.size() - windowSize, 0);
        for (int i = start; i < sorted.size(); i++) {
            Message message = sorted.get(i);
            AiChatDTO.AiChatMessage aiMessage = new AiChatDTO.AiChatMessage();
            aiMessage.setRole(message.getSenderId().equals(userId) ? "user" : "assistant");
            aiMessage.setContent(message.getContent());
            result.add(aiMessage);
        }

        return result;
    }

    public void updateSummaryIfNeeded(Long userId,
                                      Long robotId,
                                      List<Message> historyMessages,
                                      int summaryTriggerSize,
                                      int windowSize,
                                      Long currentMessageId) {
        if (historyMessages == null || historyMessages.size() < summaryTriggerSize) {
            return;
        }

        List<Message> sorted = historyMessages.stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .toList();

        int compactCount = Math.max(sorted.size() - windowSize, 0);
        if (compactCount <= 0) {
            return;
        }

        String summaryText = aiUsagePolicy.buildSimpleSummary(sorted.subList(0, compactCount), userId);
        if (summaryText.isBlank()) {
            return;
        }

        aiChatSummaryMapper.upsert(userId, robotId, summaryText, currentMessageId);
    }

    /**
     * Persists one classified memory row with embedding (insert-only).
     */
    public void insertMemory(Long userId, Long robotId, AiMemoryType type, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        AiMemoryType resolved = type != null ? type : AiMemoryType.FACT;
        List<Double> embedding = embeddingVectorClient.embed(content);
        String embeddingVector = null;
        if (embedding != null && !embedding.isEmpty()) {
            try {
                embeddingVector = objectMapper.writeValueAsString(embedding);
            } catch (Exception ignored) {
                embeddingVector = null;
            }
        }
        aiMemoryMapper.insert(userId, robotId, resolved, content.trim(), embeddingVector);
    }

    /**
     * Tool result payload for the LLM (plain text lines).
     */
    public String searchMemoriesForTool(Long userId, Long robotId, String query, int limit) {
        int cap = limit > 0 ? Math.min(limit, 20) : DEFAULT_RAG_TOP_K;
        List<AiMemory> memories = fetchRagMemories(userId, robotId, query, cap);
        if (memories.isEmpty()) {
            return "(无相关记忆)";
        }
        return memories.stream()
                .map(mem -> "[" + mem.getType().name() + "] " + mem.getContent())
                .collect(Collectors.joining("\n"));
    }

    private List<AiMemory> fetchRagMemories(Long userId, Long robotId, String query, int ragTopK) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return distinctMemories(aiMemoryMapper.selectLatest(userId, robotId, DEFAULT_MAX_MEMORY_ITEMS), ragTopK);
        }

        List<Double> queryEmbedding = embeddingVectorClient.embed(normalizedQuery);
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return distinctMemories(aiMemoryMapper.selectLatest(userId, robotId, DEFAULT_MAX_MEMORY_ITEMS), ragTopK);
        }
        String embeddingVector;
        try {
            embeddingVector = objectMapper.writeValueAsString(queryEmbedding);
        } catch (Exception e) {
            return distinctMemories(aiMemoryMapper.selectLatest(userId, robotId, DEFAULT_MAX_MEMORY_ITEMS), ragTopK);
        }
        return distinctMemories(aiMemoryMapper.selectTopByEmbedding(userId, robotId, embeddingVector, ragTopK), ragTopK);
    }

    private List<AiMemory> distinctMemories(List<AiMemory> memories, int ragTopK) {
        if (memories == null || memories.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new HashSet<>();
        List<AiMemory> result = new ArrayList<>();
        for (AiMemory memory : memories) {
            if (memory.getContent() == null) {
                continue;
            }
            String normalized = memory.getContent().trim();
            if (normalized.isEmpty() || !seen.add(normalized)) {
                continue;
            }
            result.add(memory);
            if (result.size() >= ragTopK) {
                break;
            }
        }
        return result;
    }
}
