package org.instalk.cloud.instalkaiservice.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class AiChatService {

    private final Map<Long, Set<String>> userTasksMap = new HashMap<>();

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
        int ragTopK = resolvePositiveOrDefault(aiChatDTO.getRagTopK(), AiContextService.DEFAULT_RAG_TOP_K);
        boolean includeSummary = aiChatDTO.getIncludeSummary() == null || aiChatDTO.getIncludeSummary();
        boolean includeRag = aiChatDTO.getIncludeRag() == null || aiChatDTO.getIncludeRag();

        int historyLimit = Math.max(windowSize, summaryTriggerSize);

        List<Message> historyMessages = resolveHistoryMessages(aiChatDTO, userId, robotId, historyLimit);

        List<AiChatDTO.AiChatMessage> contextMessages = aiContextService.buildContext(
                userId,
                robotId,
                historyMessages,
                userMessage.getContent(),
                windowSize,
                ragTopK,
                includeSummary,
                includeRag
        );

        MessageVO messageVO = new MessageVO(userMessage, false);
        webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(robotId, messageVO));

        List<ChatMessage> lcMessages = new ArrayList<>();
        if (userAiConfig.getSystemPrompt() != null && !userAiConfig.getSystemPrompt().isBlank()) {
            lcMessages.add(SystemMessage.from(userAiConfig.getSystemPrompt()));
        }
        lcMessages.addAll(InstalkChatMessages.forChatCompletion(contextMessages, userMessage.getContent()));

        ChatRequest chatRequest = userAiChatRequestFactory.chatRequest(lcMessages, userAiConfig);

        StringBuilder fullResponse = new StringBuilder();

        streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse == null || partialResponse.isEmpty()) {
                    return;
                }
                fullResponse.append(partialResponse);
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
                fullResponse.append(text);
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
                        summaryTriggerSize, windowSize, fullResponse.toString()), ForkJoinPool.commonPool());
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
                cleanupTask(userId, taskId);
            }
        });

        emitter.onTimeout(() -> {
            emitter.complete();
            cleanupTask(userId, taskId);
        });

        emitter.onCompletion(() -> cleanupTask(userId, taskId));

        return emitter;
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
                                       String fullResponseText) {
        try {
            Message assistantMessage = new Message();
            assistantMessage.setSenderId(robotId);
            assistantMessage.setReceiverId(userId);
            assistantMessage.setMessageType("TEXT");
            assistantMessage.setContent(fullResponseText);

            assistantMessage = messageFeignClient.addPrivateMessage(assistantMessage);

            messageFeignClient.addStatus(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getReceiverId()));
            messageFeignClient.addStatusAndRead(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getSenderId()));

            userAiConfigMapper.increaseMessageCount(robotId);
            userAiConfigMapper.increaseTokenCount(robotId, aiUsagePolicy.estimateTokenCount(fullResponseText));

            List<Message> summarySource = new ArrayList<>(historyMessages == null ? List.of() : historyMessages);
            if (summarySource.stream().noneMatch(message -> Objects.equals(message.getId(), userMessage.getId()))) {
                summarySource.add(userMessage);
            }
            summarySource.add(assistantMessage);
            aiContextService.updateSummaryIfNeeded(userId, robotId, summarySource, summaryTriggerSize, windowSize, assistantMessage.getId());
            aiContextService.upsertMemory(userId, robotId, userMessage.getContent());

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
