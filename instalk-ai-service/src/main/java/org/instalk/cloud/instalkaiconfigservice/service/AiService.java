package org.instalk.cloud.instalkaiconfigservice.service;

import org.instalk.cloud.common.feign.client.FriendshipFeignClient;
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
import org.instalk.cloud.instalkaiconfigservice.mapper.UserAiConfigMapper;
import org.instalk.cloud.instalkaiconfigservice.service.AiContextService;
import org.instalk.cloud.instalkaiconfigservice.util.AiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.util.*;

@Service
public class AiService {

    private final Map<Long, Set<String>> userTasksMap = new HashMap<>();

    @Autowired
    private WebClient aiWebClient;

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private MessageFeignClient messageFeignClient;

    @Autowired
    private WebSocketFeignClient webSocketFeignClient;

    @Autowired
    private AiContextService aiContextService;

    public Result<String> getCredential() {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Long userId = ThreadLocalUtil.getId();
        if (userTasksMap.containsKey(userId)){
            userTasksMap.get(userId).add(taskId);
        }else{
            Set<String> taskSet = new HashSet<>();
            taskSet.add(taskId);
            userTasksMap.put(userId, taskSet);
        }
        return Result.success(taskId);
    }


    public SseEmitter streamChat(AiChatDTO aiChatDTO) {
        Long userId = ThreadLocalUtil.getId();

        // 验证taskId
        String taskId = aiChatDTO.getTaskId();
        if (taskId == null || !userTasksMap.containsKey(userId) || !userTasksMap.get(userId).contains(taskId)) {
            throw new RuntimeException("无效的任务ID");
        }

        Long robotId = aiChatDTO.getRobotId();
        if (robotId == null) {
            throw new RuntimeException("对话不存在");
        }

        // 获取用户AI配置
        UserAiConfig userAiConfig = userAiConfigMapper.select(robotId);
        if (userAiConfig == null) {
            throw new RuntimeException("AI配置不存在");
        }

        if (aiUtil.needsReset(userAiConfig)){
            userAiConfigMapper.resetMessageCount(robotId);
        }

        // 检查消息限制
        if (!aiUtil.canSendMessage(userAiConfig)) {
            throw new RuntimeException("已达到每日消息限制");
        }

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 获取用户当前消息
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

        // 获取历史消息
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

        // 将用户消息通过 WebSocket 发送给 AI Robot 用户
        MessageVO messageVO = new MessageVO(userMessage, false);
        webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(robotId, messageVO));

        // 构建请求体
        String requestBody = aiUtil.buildRequestBody(contextMessages, userAiConfig, userMessage.getContent());

        // 用于累积AI的完整回复
        StringBuilder fullResponse = new StringBuilder();

        // 异步调用AI接口
        aiWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    try {
                        // 解析流式响应
                        String content = aiUtil.parseStreamResponse(chunk);
                        if (content != null && !content.isEmpty()) {
                            fullResponse.append(content);
                            // 发送到前端（将 \n 转义为字面量 \n，防止 SSE 换行拆包导致前端丢失换行）
                            emitter.send(SseEmitter.event()
                                    .data(content.replace("\n", "\\n"))
                                    .name("message"));
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    try {
                        // 保存AI回复
                        Message assistantMessage = new Message();
                        assistantMessage.setSenderId(robotId);
                        assistantMessage.setReceiverId(userId);
                        assistantMessage.setMessageType("TEXT");
                        assistantMessage.setContent(fullResponse.toString());

                        assistantMessage = messageFeignClient.addPrivateMessage(assistantMessage);

                        messageFeignClient.addStatus(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getReceiverId()));
                        messageFeignClient.addStatusAndRead(new MessageStatusDTO(assistantMessage.getId(), assistantMessage.getSenderId()));

                        // 增加消息计数
                        userAiConfigMapper.increaseMessageCount(robotId);
                        //增加token使用
                        userAiConfigMapper.increaseTokenCount(robotId,aiUtil.estimateTokenCount(fullResponse.toString()));

                        // 更新摘要与记忆
                        List<Message> summarySource = new ArrayList<>(historyMessages == null ? List.of() : historyMessages);
                        if (summarySource.stream().noneMatch(message -> Objects.equals(message.getId(), userMessage.getId()))) {
                            summarySource.add(userMessage);
                        }
                        summarySource.add(assistantMessage);
                        aiContextService.updateSummaryIfNeeded(userId, robotId, summarySource, summaryTriggerSize, windowSize, assistantMessage.getId());
                        aiContextService.upsertMemory(userId, robotId, userMessage.getContent());

                        // 通过 WebSocket 将 AI 回复推送给用户和AI自己
                        MessageVO aiMessageVO = new MessageVO(messageFeignClient.getById(assistantMessage.getId()), messageFeignClient.getStatus(new MessageStatusDTO(assistantMessage.getId(), userId)));
                        webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(userId, aiMessageVO));
                        aiMessageVO.setIsRead(messageFeignClient.getStatus(new MessageStatusDTO(assistantMessage.getId(), robotId)));
                        webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(robotId, aiMessageVO));

                        // 发送完成信号到 SSE
                        emitter.send(SseEmitter.event()
                                .data("[DONE]")
                                .name("done"));
                        emitter.complete();

                        // 清理taskId
                        if (userTasksMap.containsKey(userId)) {
                            userTasksMap.get(userId).remove(taskId);
                            if (userTasksMap.get(userId).isEmpty()) {
                                userTasksMap.remove(userId);
                            }
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(error -> {
                    emitter.completeWithError(error);
                    // 清理taskId
                    if (userTasksMap.containsKey(userId)) {
                        userTasksMap.get(userId).remove(taskId);
                        if (userTasksMap.get(userId).isEmpty()) {
                            userTasksMap.remove(userId);
                        }
                    }
                })
                .subscribe();

        // 设置SSE发射器的超时和完成回调
        emitter.onTimeout(() -> {
            emitter.complete();
            // 清理taskId
            if (userTasksMap.containsKey(userId)) {
                userTasksMap.get(userId).remove(taskId);
                if (userTasksMap.get(userId).isEmpty()) {
                    userTasksMap.remove(userId);
                }
            }
        });

        emitter.onCompletion(() -> {
            // 清理taskId
            if (userTasksMap.containsKey(userId)) {
                userTasksMap.get(userId).remove(taskId);
                if (userTasksMap.get(userId).isEmpty()) {
                    userTasksMap.remove(userId);
                }
            }
        });

        return emitter;
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

        // 检查是否需要重置每日计数
        if (aiUtil.needsReset(userAiConfig)) {
            userAiConfigMapper.resetMessageCount(robotId);
            // 重新查询以获取重置后的数据
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
