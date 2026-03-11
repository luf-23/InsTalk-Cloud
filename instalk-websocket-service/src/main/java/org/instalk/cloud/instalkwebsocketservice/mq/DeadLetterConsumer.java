package org.instalk.cloud.instalkwebsocketservice.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.mq.MessageMQ;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterConsumer {

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(MessageMQ messageMQ, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.error("========== 死信队列警告 ==========");
            log.error("消息ID: {}", messageMQ.getMessageVO().getId());
            log.error("消息类型: {}", messageMQ.getMessageType());
            log.error("重试次数: {}", messageMQ.getRetryCount());
            log.error("================================");
            
            // TODO: 保存到失败消息表 或 发送告警
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理死信消息异常: {}", e.getMessage());
        }
    }
}
