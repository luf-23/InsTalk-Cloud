package org.instalk.cloud.instalkchatservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;

/** WebSocket 按实例定向投递队列。 */
@Slf4j
@Configuration
public class MessageRabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true);
        template.setReturnsCallback(returned ->
                log.error("消息路由失败: 交换机={}, 路由键={}", returned.getExchange(), returned.getRoutingKey()));
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息投递失败: {}", cause);
            }
        });
        return template;
    }

    @Bean
    public DirectExchange wsPushInstanceExchange() {
        return new DirectExchange(RabbitMQConfig.WS_PUSH_INSTANCE_EXCHANGE, true, false);
    }

    @Bean
    public Queue messagePushInstanceQueue(InstanceIdProvider instanceIdProvider) {
        return QueueBuilder.durable("instalk.ws.push." + instanceIdProvider.getInstanceId())
                .autoDelete()
                .build();
    }

    @Bean
    public Binding messagePushInstanceBinding(@Qualifier("messagePushInstanceQueue") Queue messagePushInstanceQueue,
                                              InstanceIdProvider instanceIdProvider,
                                              @Qualifier("wsPushInstanceExchange") DirectExchange wsPushInstanceExchange) {
        return BindingBuilder.bind(messagePushInstanceQueue).to(wsPushInstanceExchange)
                .with(instanceIdProvider.getInstanceId());
    }

}
