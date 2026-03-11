package org.instalk.cloud.instalkmessageservice.mq;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.mq.MessageMQ;
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
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MESSAGE_EXCHANGE,
                RabbitMQConfig.PRIVATE_MESSAGE_ROUTING_KEY,
                messageMQ
            );
            log.info("私聊消息已发送到MQ, ID: {}", messageMQ.getMessageVO().getId());
        } catch (Exception e) {
            log.error("发送私聊消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    public void sendGroupMessage(MessageMQ messageMQ) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MESSAGE_EXCHANGE,
                RabbitMQConfig.GROUP_MESSAGE_ROUTING_KEY,
                messageMQ
            );
            log.info("群聊消息已发送到MQ, ID: {}, 接收人数: {}", 
                messageMQ.getMessageVO().getId(), 
                messageMQ.getReceiverIds().size());
        } catch (Exception e) {
            log.error("发送群聊消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}
