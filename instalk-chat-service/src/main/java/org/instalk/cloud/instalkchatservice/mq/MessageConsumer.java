package org.instalk.cloud.instalkchatservice.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.mq.MessagePushMQ;
import org.instalk.cloud.instalkchatservice.service.WebSocketHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 每个实例消费自己的 Fanout 队列副本，仅向本地 WebSocket session 投递。
 */
@Slf4j
@Component
public class MessageConsumer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @RabbitListener(queues = "#{messagePushInstanceQueue.name}")
    public void handleMessagePush(MessagePushMQ messagePushMQ, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
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
            case USER_ONLINE_STATUS -> handleOnlineStatus(messagePushMQ);
            default -> log.warn("未知的 WebSocket 推送类型: {}", messagePushMQ.getPushType());
        }
    }

    private void handlePrivateMessage(MessagePushMQ messagePushMQ) {
        Long receiverId = messagePushMQ.getReceiverId();
        if (webSocketHandler.hasLocalSession(receiverId)) {
            webSocketHandler.sendMessageToUser(receiverId, messagePushMQ.getMessageVO());
            log.debug("本实例已推送私聊消息给用户 {}, 消息ID: {}", receiverId, messagePushMQ.getMessageVO().getId());
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

    private void handleOnlineStatus(MessagePushMQ messagePushMQ) {
        webSocketHandler.broadcastOnlineStatusChange(
                messagePushMQ.getReceiverId(), Boolean.TRUE.equals(messagePushMQ.getOnline()));
    }

    private void handleError(MessagePushMQ messagePushMQ, Channel channel, long deliveryTag) {
        try {
            messagePushMQ.setRetryCount(messagePushMQ.getRetryCount() + 1);
            if (messagePushMQ.getRetryCount() < 3) {
                channel.basicNack(deliveryTag, false, true);
                log.warn("WebSocket 推送重试, type={}, 次数: {}", messagePushMQ.getPushType(), messagePushMQ.getRetryCount());
            } else {
                channel.basicNack(deliveryTag, false, false);
                log.error("WebSocket 推送重试超限, type={}", messagePushMQ.getPushType());
            }
        } catch (IOException e) {
            log.error("WebSocket 推送 ACK 处理失败: {}", e.getMessage());
        }
    }
}
