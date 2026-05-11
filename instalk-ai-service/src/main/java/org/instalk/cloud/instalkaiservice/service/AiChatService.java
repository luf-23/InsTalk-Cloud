package org.instalk.cloud.instalkaiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.instalkaiservice.llm.AiMemoryTools;
import org.instalk.cloud.instalkaiservice.llm.InstalkChatMessages;
import org.instalk.cloud.instalkaiservice.llm.UserAiChatRequestFactory;
import org.instalk.cloud.instalkaiservice.mapper.UserAiConfigMapper;
import org.instalk.cloud.common.feign.client.MessageFeignClient;
import org.instalk.cloud.common.feign.client.WebSocketFeignClient;
import org.instalk.cloud.common.model.dto.AiChatDTO;
import org.instalk.cloud.common.model.dto.UserAiConfigDTO;
import org.instalk.cloud.common.model.dto.internal.MessageStatusDTO;
import org.instalk.cloud.common.model.dto.internal.WsSendPrivateMessageDTO;
import org.instalk.cloud.common.model.po.Message;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserAiConfigVO;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AiChatService {

    private static final int MAX_MODEL_INVOCATIONS_WITH_TOOLS = 10;

    private final Map<Long, Set<String>> userTasksMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    @Autowired
    private AiUsagePolicy aiUsagePolicy;

    @Autowired
    private MessageFeignClient messageFeignClient;

    @Autowired
    private WebSocketFeignClient webSocketFeignClient;

    @Autowired
    private AiContextService aiContextService;

    @Autowired
    private AiMemoryExtractionService aiMemoryExtractionService;

    @Autowired
    private UserAiChatRequestFactory userAiChatRequestFactory;

    public Result<String> getCredential() {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Long userId = ThreadLocalUtil.getId();
        userTasksMap.computeIfAbsent(userId, k -> new HashSet<>()).add(taskId);
        return Result.success(taskId);
    }

    public SseEmitter streamChat(AiChatDTO aiChatDTO) {
        Long userId = ThreadLocalUtil.getId();

        String taskId = aiChatDTO.getTaskId();
        if (taskId == null || !userTasksMap.containsKey(userId) || !userTasksMap.get(userId).contains(taskId)) {
            throw new RuntimeException("无效的任务ID");
        }

        Long robotId = aiChatDTO.getRobotId();
        if (robotId == null) {
            throw new RuntimeException("对话不存在");
        }

        UserAiConfig userAiConfig = userAiConfigMapper.select(robotId);
        if (userAiConfig == null) {
            throw new RuntimeException("AI配置不存在");
        }

        if (aiUsagePolicy.needsReset(userAiConfig)) {
            userAiConfigMapper.resetMessageCount(robotId);
        }

        if (!aiUsagePolicy.canSendMessage(userAiConfig)) {
            throw new RuntimeException("已达到每日消息限制");
        }

        SseEmitter emitter = new SseEmitter(300000L);

        Message userMessage = messageFeignClient.getById(aiChatDTO.getCurrentUserMessageId());
        if (userMessage == null) {
            throw new RuntimeException("当前消息不存在");
        }

        int windowSize = resolvePositiveOrDefault(aiChatDTO.getWindowSize(), AiContextService.DEFAULT_WINDOW_SIZE);
        int summaryTriggerSize = resolvePositiveOrDefault(aiChatDTO.getSummaryTriggerSize(), AiContextService.DEFAULT_SUMMARY_TRIGGER_SIZE);
        boolean includeSummary = aiChatDTO.getIncludeSummary() == null || aiChatDTO.getIncludeSummary();

        int historyLimit = Math.max(windowSize, summaryTriggerSize);

        List<Message> historyMessages = resolveHistoryMessages(aiChatDTO, userId, robotId, historyLimit);

        List<AiChatDTO.AiChatMessage> contextMessages = aiContextService.buildContext(
                userId,
                robotId,
                historyMessages,
                windowSize,
                includeSummary
        );

        MessageVO messageVO = new MessageVO(userMessage, false);
        webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(robotId, messageVO));

        List<ChatMessage> lcMessages = new ArrayList<>();
        if (userAiConfig.getSystemPrompt() != null && !userAiConfig.getSystemPrompt().isBlank()) {
            lcMessages.add(SystemMessage.from(userAiConfig.getSystemPrompt()));
        }
        lcMessages.add(SystemMessage.from(
                "按需使用工具 search_memories：仅当回答依赖用户过往事实、偏好或事件时再检索；泛泛寒暄或无需个人上下文时不要调用。"));
        lcMessages.addAll(InstalkChatMessages.forChatCompletion(contextMessages, userMessage.getContent()));

        StringBuilder assistantVisibleText = new StringBuilder();
        AtomicInteger modelInvocationCount = new AtomicInteger(0);

        streamChatWithMemoryTools(
                userId,
                robotId,
                taskId,
                userAiConfig,
                lcMessages,
                emitter,
                assistantVisibleText,
                modelInvocationCount,
                () -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .data("[DONE]")
                                .name("done"));
                    } catch (Exception e) {
                        log.debug("SSE 发送 DONE 失败（客户端可能已断开）: {}", e.getMessage());
                    }
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.debug("SSE complete 失败: {}", e.getMessage());
                    }
                    CompletableFuture.runAsync(() -> persistAssistantReply(userId, robotId, userMessage, historyMessages,
                            summaryTriggerSize, windowSize, assistantVisibleText.toString()), ForkJoinPool.commonPool());
                },
                error -> {
                    emitter.completeWithError(error);
                    cleanupTask(userId, taskId);
                }
        );

        emitter.onTimeout(() -> {
            emitter.complete();
            cleanupTask(userId, taskId);
        });

        emitter.onCompletion(() -> cleanupTask(userId, taskId));

        return emitter;
    }

    private ChatRequest chatRequestWithTools(List<ChatMessage> lcMessages, UserAiConfig userAiConfig) {
        return userAiChatRequestFactory.chatRequest(lcMessages, userAiConfig, AiMemoryTools.all());
    }

    private void streamChatWithMemoryTools(Long userId,
                                           Long robotId,
                                           String taskId,
                                           UserAiConfig userAiConfig,
                                           List<ChatMessage> lcMessages,
                                           SseEmitter emitter,
                                           StringBuilder assistantVisibleText,
                                           AtomicInteger modelInvocationCount,
                                           Runnable onFinishedSuccess,
                                           java.util.function.Consumer<Throwable> onFatalError) {
        if (modelInvocationCount.incrementAndGet() > MAX_MODEL_INVOCATIONS_WITH_TOOLS) {
            onFatalError.accept(new RuntimeException("工具调用轮次过多"));
            return;
        }

        ChatRequest chatRequest = chatRequestWithTools(lcMessages, userAiConfig);

        streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse == null || partialResponse.isEmpty()) {
                    return;
                }
                assistantVisibleText.append(partialResponse);
                try {
                    emitter.send(SseEmitter.event()
                            .data(partialResponse.replace("\n", "\\n"))
                            .name("message"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                if (partialThinking == null) {
                    return;
                }
                String text = partialThinking.text();
                if (text == null || text.isEmpty()) {
                    return;
                }
                try {
                    emitter.send(SseEmitter.event()
                            .data(text.replace("\n", "\\n"))
                            .name("message"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                AiMessage aiMessage = completeResponse.aiMessage();
                if (aiMessage != null && aiMessage.hasToolExecutionRequests()) {
                    lcMessages.add(aiMessage);
                    for (Object o : aiMessage.toolExecutionRequests()) {
                        if (!(o instanceof ToolExecutionRequest req)) {
                            continue;
                        }
                        String toolResult = executeMemoryTool(userId, robotId, req);
                        lcMessages.add(ToolExecutionResultMessage.from(req, toolResult));
                    }
                    streamChatWithMemoryTools(userId, robotId, taskId, userAiConfig, lcMessages, emitter,
                            assistantVisibleText, modelInvocationCount, onFinishedSuccess, onFatalError);
                    return;
                }
                onFinishedSuccess.run();
            }

            @Override
            public void onError(Throwable error) {
                onFatalError.accept(error);
            }
        });
    }

    private String executeMemoryTool(Long userId, Long robotId, ToolExecutionRequest request) {
        if (!AiMemoryTools.SEARCH_MEMORIES.name().equals(request.name())) {
            return "未知工具";
        }
        try {
            JsonNode args = objectMapper.readTree(request.arguments() == null ? "{}" : request.arguments());
            String query = args.hasNonNull("query") ? args.get("query").asText("") : "";
            int limit = AiContextService.DEFAULT_RAG_TOP_K;
            if (args.hasNonNull("limit") && args.get("limit").canConvertToInt()) {
                limit = args.get("limit").asInt();
            }
            return aiContextService.searchMemoriesForTool(userId, robotId, query, limit);
        } catch (Exception e) {
            log.debug("search_memories 参数解析失败", e);
            return "(参数解析失败)";
        }
    }

    private void cleanupTask(Long userId, String taskId) {
        Set<String> tasks = userTasksMap.get(userId);
        if (tasks == null) {
            return;
        }
        tasks.remove(taskId);
        if (tasks.isEmpty()) {
            userTasksMap.remove(userId);
        }
    }

    private void persistAssistantReply(Long userId, Long robotId, Message userMessage,
                                       List<Message> historyMessages, int summaryTriggerSize, int windowSize,
                                       String assistantPlainText) {
        try {
            Message assistantMessage = new Message();
            assistantMessage.setSenderId(robotId);
            assistantMessage.setReceiverId(userId);
            assistantMessage.setMessageType("TEXT");
            assistantMessage.setContent(assistantPlainText);

            assistantMessage = messageFeignClient.addPrivateMessage(assistantMessage);

            messageFeignClient.addStatus(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getReceiverId()));
            messageFeignClient.addStatusAndRead(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getSenderId()));

            userAiConfigMapper.increaseMessageCount(robotId);
            userAiConfigMapper.increaseTokenCount(robotId, aiUsagePolicy.estimateTokenCount(assistantPlainText));

            List<Message> summarySource = new ArrayList<>(historyMessages == null ? List.of() : historyMessages);
            if (summarySource.stream().noneMatch(message -> Objects.equals(message.getId(), userMessage.getId()))) {
                summarySource.add(userMessage);
            }
            summarySource.add(assistantMessage);
            aiContextService.updateSummaryIfNeeded(userId, robotId, summarySource, summaryTriggerSize, windowSize, assistantMessage.getId());
            aiMemoryExtractionService.extractAndPersist(userId, robotId, userMessage.getContent(), assistantPlainText);

            MessageVO aiMessageVO = new MessageVO(messageFeignClient.getById(assistantMessage.getId()),
                    messageFeignClient.getStatus(new MessageStatusDTO(assistantMessage.getId(), userId)));
            webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(userId, aiMessageVO));
            aiMessageVO.setIsRead(messageFeignClient.getStatus(new MessageStatusDTO(assistantMessage.getId(), robotId)));
            webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(robotId, aiMessageVO));
        } catch (Exception e) {
            log.error("持久化助手回复失败 userId={} robotId={}", userId, robotId, e);
        }
    }

    private int resolvePositiveOrDefault(Integer value, int defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private List<Message> resolveHistoryMessages(AiChatDTO aiChatDTO, Long userId, Long robotId, int limit) {
        List<Long> messageIds = aiChatDTO.getMessageIds();
        if (messageIds != null && !messageIds.isEmpty()) {
            return messageFeignClient.getByIds(messageIds);
        }
        return messageFeignClient.getPrivateHistory(userId, robotId, limit);
    }

    public Result<Void> update(UserAiConfigDTO userAiConfigDTO) {
        UserAiConfig userAiConfig = userAiConfigMapper.select(userAiConfigDTO.getRobotId());
        UserAiConfig newUserAiConfig = new UserAiConfig(userAiConfig, userAiConfigDTO);
        userAiConfigMapper.update(newUserAiConfig);
        return Result.success(null);
    }

    public Result<UserAiConfigVO> getAiConfig(Long robotId) {
        UserAiConfig userAiConfig = userAiConfigMapper.selectByRobotId(robotId);

        if (aiUsagePolicy.needsReset(userAiConfig)) {
            userAiConfigMapper.resetMessageCount(robotId);
            userAiConfig = userAiConfigMapper.selectByRobotId(robotId);
        }

        UserAiConfigVO userAiConfigVO = new UserAiConfigVO();
        userAiConfigVO.setSystemPrompt(userAiConfig.getSystemPrompt());
        userAiConfigVO.setModel(userAiConfig.getModel());
        userAiConfigVO.setTemperature(userAiConfig.getTemperature());
        userAiConfigVO.setMaxTokens(userAiConfig.getMaxTokens());
        userAiConfigVO.setTopP(userAiConfig.getTopP());
        userAiConfigVO.setPresencePenalty(userAiConfig.getPresencePenalty());
        userAiConfigVO.setSeed(userAiConfig.getSeed());
        userAiConfigVO.setDailyMessageLimit(userAiConfig.getDailyMessageLimit());
        userAiConfigVO.setDailyMessageCount(userAiConfig.getDailyMessageCount());
        userAiConfigVO.setLastResetDate(userAiConfig.getLastResetDate());
        userAiConfigVO.setTotalMessages(userAiConfig.getTotalMessages());
        userAiConfigVO.setTotalTokensUsed(userAiConfig.getTotalTokensUsed());
        userAiConfigVO.setLastUsedAt(userAiConfig.getLastUsedAt());
        return Result.success(userAiConfigVO);
    }
}
