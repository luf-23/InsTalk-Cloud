package org.instalk.cloud.instalkchatservice.mq;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastGroupDeleteDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastRevokeDTO;
import org.instalk.cloud.common.model.dto.internal.WsDeleteFriendDTO;
import org.instalk.cloud.common.model.dto.internal.WsRevokeMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsSendPrivateMessageDTO;
import org.instalk.cloud.common.model.mq.MessageMQ;
import org.instalk.cloud.common.model.mq.MessagePushMQ;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendPrivateMessage(MessageMQ messageMQ) {
        publish(MessagePushMQ.fromPrivateMessage(messageMQ));
        log.info("私聊 WebSocket 推送已广播到 Fanout, 消息ID: {}", messageMQ.getMessageVO().getId());
    }

    public void sendGroupMessage(MessageMQ messageMQ) {
        publish(MessagePushMQ.fromGroupMessage(messageMQ));
        log.info("群聊 WebSocket 推送已广播到 Fanout, 消息ID: {}", messageMQ.getMessageVO().getId());
    }

    public void publishSendPrivateMessage(WsSendPrivateMessageDTO dto) {
        publish(MessagePushMQ.fromSendPrivateMessage(dto));
    }

    public void publishBroadcastMessage(WsBroadcastMessageDTO dto) {
        publish(MessagePushMQ.fromBroadcastMessage(dto));
    }

    public void publishFriendDeleted(WsDeleteFriendDTO dto) {
        publish(MessagePushMQ.fromDeleteFriend(dto));
    }

    public void publishMessageRecall(WsRevokeMessageDTO dto) {
        publish(MessagePushMQ.fromRevokeMessage(dto));
    }

    public void publishBroadcastRecall(WsBroadcastRevokeDTO dto) {
        publish(MessagePushMQ.fromBroadcastRevoke(dto));
    }

    public void publishGroupDeleted(WsBroadcastGroupDeleteDTO dto) {
        publish(MessagePushMQ.fromBroadcastGroupDelete(dto));
    }

    public void publishOnlineStatus(Long userId, boolean online) {
        publish(MessagePushMQ.fromOnlineStatus(userId, online));
    }

    private void publish(MessagePushMQ messagePushMQ) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.WS_PUSH_FANOUT_EXCHANGE, "", messagePushMQ);
        } catch (Exception e) {
            log.error("WebSocket 推送广播失败, type={}: {}", messagePushMQ.getPushType(), e.getMessage(), e);
            throw new RuntimeException("WebSocket 推送广播失败", e);
        }
    }
}
