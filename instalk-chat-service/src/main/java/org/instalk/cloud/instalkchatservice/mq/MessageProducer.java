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
import org.instalk.cloud.instalkchatservice.service.WsOnlineRegistryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WsOnlineRegistryService wsOnlineRegistry;

    public void sendPrivateMessage(MessageMQ messageMQ) {
        publishToUser(MessagePushMQ.fromPrivateMessage(messageMQ));
        log.info("私聊 WebSocket 推送已定向投递, 消息ID: {}", messageMQ.getMessageVO().getId());
    }

    public void sendGroupMessage(MessageMQ messageMQ) {
        publishToUsers(MessagePushMQ.fromGroupMessage(messageMQ));
        log.info("群聊 WebSocket 推送已按实例定向投递, 消息ID: {}", messageMQ.getMessageVO().getId());
    }

    public void publishSendPrivateMessage(WsSendPrivateMessageDTO dto) {
        publishToUser(MessagePushMQ.fromSendPrivateMessage(dto));
    }

    public void publishBroadcastMessage(WsBroadcastMessageDTO dto) {
        publishToUsers(MessagePushMQ.fromBroadcastMessage(dto));
    }

    public void publishFriendDeleted(WsDeleteFriendDTO dto) {
        publishToUser(MessagePushMQ.fromDeleteFriend(dto));
    }

    public void publishMessageRecall(WsRevokeMessageDTO dto) {
        publishToUser(MessagePushMQ.fromRevokeMessage(dto));
    }

    public void publishBroadcastRecall(WsBroadcastRevokeDTO dto) {
        publishToUsers(MessagePushMQ.fromBroadcastRevoke(dto));
    }

    public void publishGroupDeleted(WsBroadcastGroupDeleteDTO dto) {
        publishToUsers(MessagePushMQ.fromBroadcastGroupDelete(dto));
    }

    private void publishToUser(MessagePushMQ messagePushMQ) {
        Long userId = messagePushMQ.getReceiverId();
        for (String instanceId : wsOnlineRegistry.findInstanceIds(userId)) {
            publishToInstance(instanceId, messagePushMQ);
        }
    }

    private void publishToUsers(MessagePushMQ messagePushMQ) {
        Map<String, List<Long>> usersByInstance = new LinkedHashMap<>();
        for (Long userId : messagePushMQ.getReceiverIds()) {
            for (String instanceId : wsOnlineRegistry.findInstanceIds(userId)) {
                usersByInstance.computeIfAbsent(instanceId, key -> new ArrayList<>()).add(userId);
            }
        }
        usersByInstance.forEach((instanceId, userIds) ->
                publishToInstance(instanceId, messagePushMQ.forReceivers(userIds)));
    }

    private void publishToInstance(String instanceId, MessagePushMQ messagePushMQ) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.WS_PUSH_INSTANCE_EXCHANGE, instanceId, messagePushMQ);
        } catch (Exception e) {
            log.error("WebSocket 推送定向投递失败, instanceId={}, type={}: {}", instanceId,
                    messagePushMQ.getPushType(), e.getMessage(), e);
            throw new RuntimeException("WebSocket 推送定向投递失败", e);
        }
    }
}
