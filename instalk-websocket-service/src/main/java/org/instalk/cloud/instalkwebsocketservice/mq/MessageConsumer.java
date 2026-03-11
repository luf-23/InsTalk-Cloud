package org.instalk.cloud.instalkwebsocketservice.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.mq.MessageMQ;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;
import org.instalk.cloud.instalkwebsocketservice.service.WebSocketHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MessageConsumer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @RabbitListener(queues = RabbitMQConfig.PRIVATE_MESSAGE_QUEUE)
    public void handlePrivateMessage(MessageMQ messageMQ, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Long messageId = messageMQ.getMessageVO().getId();
        Long receiverId = messageMQ.getMessageVO().getReceiverId();
        
        try {
            if (webSocketHandler.isUserOnline(receiverId)) {
                webSocketHandler.sendMessageToUser(receiverId, messageMQ.getMessageVO());
                channel.basicAck(deliveryTag, false);
                log.info("私聊消息已送达, ID: {}", messageId);
            } else {
                log.warn("用户{}离线, 消息重新入队, ID: {}", receiverId, messageId);
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            log.error("处理私聊消息失败, ID: {}", messageId, e);
            handleError(messageMQ, channel, deliveryTag);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.GROUP_MESSAGE_QUEUE)
    public void handleGroupMessage(MessageMQ messageMQ, Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Long messageId = messageMQ.getMessageVO().getId();
        
        try {
            int successCount = 0;
            for (Long receiverId : messageMQ.getReceiverIds()) {
                if (webSocketHandler.isUserOnline(receiverId)) {
                    try {
                        webSocketHandler.sendMessageToUser(receiverId, messageMQ.getMessageVO());
                        successCount++;
                    } catch (Exception e) {
                        log.warn("推送给用户{}失败", receiverId);
                    }
                }
            }
            
            if (successCount > 0) {
                channel.basicAck(deliveryTag, false);
                log.info("群消息推送完成, ID: {}, 成功: {}/{}", messageId, successCount, messageMQ.getReceiverIds().size());
            } else {
                channel.basicNack(deliveryTag, false, true);
                log.warn("群消息无人在线, 重新入队, ID: {}", messageId);
            }
        } catch (Exception e) {
            log.error("处理群聊消息失败, ID: {}", messageId, e);
            handleError(messageMQ, channel, deliveryTag);
        }
    }

    private void handleError(MessageMQ messageMQ, Channel channel, long deliveryTag) {
        try {
            messageMQ.setRetryCount(messageMQ.getRetryCount() + 1);
            if (messageMQ.getRetryCount() < 3) {
                channel.basicNack(deliveryTag, false, true);
                log.warn("消息重试, 次数: {}", messageMQ.getRetryCount());
            } else {
                channel.basicNack(deliveryTag, false, false);
                log.error("消息重试超限, 进入死信队列");
            }
        } catch (IOException e) {
            log.error("ACK处理失败: {}", e.getMessage());
        }
    }
}
