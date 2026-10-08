package org.instalk.cloud.instalkchatservice.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.mq.MessagePushMQ;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;
import org.instalk.cloud.instalkchatservice.config.InstanceIdProvider;
import org.instalk.cloud.instalkchatservice.service.WebSocketHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 消费本实例的定向 WebSocket 推送。 */
@Slf4j
@Component
public class MessageConsumer {

    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private InstanceIdProvider instanceIdProvider;

    @RabbitListener(queues = "#{messagePushInstanceQueue.name}")
    public void handleMessagePush(MessagePushMQ messagePushMQ, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handle(messagePushMQ, channel, deliveryTag);
    }

    private void handle(MessagePushMQ messagePushMQ, Channel channel, long deliveryTag) {
        try {
            dispatch(messagePushMQ);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("WebSocket 推送处理失败, type={}: {}", messagePushMQ.getPushType(), e.getMessage(), e);
            handleError(messagePushMQ, channel, deliveryTag);
        }
    }

    private void dispatch(MessagePushMQ messagePushMQ) {
        switch (messagePushMQ.getPushType()) {
            case PRIVATE_MESSAGE -> handlePrivateMessage(messagePushMQ);
            case GROUP_MESSAGE -> handleGroupMessage(messagePushMQ);
            case FRIEND_DELETED -> handleFriendDeleted(messagePushMQ);
            case MESSAGE_RECALL -> handleMessageRecall(messagePushMQ);
            case BROADCAST_RECALL -> handleBroadcastRecall(messagePushMQ);
            case GROUP_DELETED -> handleGroupDeleted(messagePushMQ);
            default -> log.warn("未知的 WebSocket 推送类型: {}", messagePushMQ.getPushType());
        }
    }

    private void handlePrivateMessage(MessagePushMQ messagePushMQ) {
        for (Long receiverId : messagePushMQ.getReceiverIds()) {
            if (webSocketHandler.hasLocalSession(receiverId)) {
                webSocketHandler.sendMessageToUser(receiverId, messagePushMQ.getMessageVO());
                log.debug("本实例已推送私聊消息给用户 {}, 消息ID: {}", receiverId, messagePushMQ.getMessageVO().getId());
            }
        }
    }

    private void handleGroupMessage(MessagePushMQ messagePushMQ) {
        int pushed = 0;
        for (Long receiverId : messagePushMQ.getReceiverIds()) {
            if (webSocketHandler.hasLocalSession(receiverId)) {
                webSocketHandler.sendMessageToUser(receiverId, messagePushMQ.getMessageVO());
                pushed++;
            }
        }
        if (pushed > 0) {
            log.debug("本实例已推送群聊消息, 消息ID: {}, 推送人数: {}/{}",
                    messagePushMQ.getMessageVO().getId(), pushed, messagePushMQ.getReceiverIds().size());
        }
    }

    private void handleFriendDeleted(MessagePushMQ messagePushMQ) {
        if (webSocketHandler.hasLocalSession(messagePushMQ.getReceiverId())) {
            webSocketHandler.sendFriendDeletedNotification(messagePushMQ.getReceiverId(), messagePushMQ.getFriendId());
        }
    }

    private void handleMessageRecall(MessagePushMQ messagePushMQ) {
        if (webSocketHandler.hasLocalSession(messagePushMQ.getReceiverId())) {
            webSocketHandler.sendMessageRecallNotification(messagePushMQ.getReceiverId(), messagePushMQ.getMessageId());
        }
    }

    private void handleBroadcastRecall(MessagePushMQ messagePushMQ) {
        webSocketHandler.broadcastMessageRecallNotification(messagePushMQ.getReceiverIds(), messagePushMQ.getMessageId());
    }

    private void handleGroupDeleted(MessagePushMQ messagePushMQ) {
        webSocketHandler.broadcastGroupDeletedNotification(messagePushMQ.getReceiverIds(), messagePushMQ.getGroupId());
    }

    private void handleError(MessagePushMQ messagePushMQ, Channel channel, long deliveryTag) {
        try {
            int retryCount = messagePushMQ.getRetryCount() == null
                    ? 1
                    : messagePushMQ.getRetryCount() + 1;
            messagePushMQ.setRetryCount(retryCount);
            if (messagePushMQ.getRetryCount() <= MAX_RETRY_COUNT) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.WS_PUSH_RETRY_EXCHANGE,
                        instanceIdProvider.getInstanceId(),
                        messagePushMQ);
                channel.basicAck(deliveryTag, false);
                log.warn("WebSocket 推送进入延迟重试, type={}, 次数: {}",
                        messagePushMQ.getPushType(), messagePushMQ.getRetryCount());
            } else {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.WS_PUSH_DEAD_LETTER_EXCHANGE,
                        RabbitMQConfig.WS_PUSH_DEAD_LETTER_ROUTING_KEY,
                        messagePushMQ);
                channel.basicAck(deliveryTag, false);
                log.error("WebSocket 推送进入死信队列, type={}, 重试次数: {}",
                        messagePushMQ.getPushType(), messagePushMQ.getRetryCount());
            }
        } catch (Exception exception) {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ackException) {
                log.error("WebSocket 推送失败后无法确认 RabbitMQ 消息: {}", ackException.getMessage());
            }
            log.error("WebSocket 推送转移到重试/死信队列失败", exception);
        }
    }
}
