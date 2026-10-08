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

    private static final long MAIN_QUEUE_MESSAGE_TTL_MS = 10 * 60 * 1000L;
    private static final long RETRY_QUEUE_TTL_MS = 5 * 1000L;
    private static final long RETRY_QUEUE_EXPIRE_MS = 30 * 60 * 1000L;

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
    public DirectExchange wsPushRetryExchange() {
        return new DirectExchange(RabbitMQConfig.WS_PUSH_RETRY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange wsPushDeadLetterExchange() {
        return new DirectExchange(RabbitMQConfig.WS_PUSH_DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue messagePushInstanceQueue(InstanceIdProvider instanceIdProvider) {
        return QueueBuilder.durable("instalk.ws.push.v2." + instanceIdProvider.getInstanceId())
                .ttl((int) MAIN_QUEUE_MESSAGE_TTL_MS)
                .deadLetterExchange(RabbitMQConfig.WS_PUSH_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConfig.WS_PUSH_DEAD_LETTER_ROUTING_KEY)
                .autoDelete()
                .build();
    }

    @Bean
    public Queue messagePushRetryQueue(InstanceIdProvider instanceIdProvider) {
        return QueueBuilder.durable("instalk.ws.push.retry.v2." + instanceIdProvider.getInstanceId())
                .ttl((int) RETRY_QUEUE_TTL_MS)
            .expires((int) RETRY_QUEUE_EXPIRE_MS)
                .deadLetterExchange(RabbitMQConfig.WS_PUSH_INSTANCE_EXCHANGE)
                .deadLetterRoutingKey(instanceIdProvider.getInstanceId())
                .build();
    }

    @Bean
    public Queue wsPushDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConfig.WS_PUSH_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding messagePushInstanceBinding(@Qualifier("messagePushInstanceQueue") Queue messagePushInstanceQueue,
                                              InstanceIdProvider instanceIdProvider,
                                              @Qualifier("wsPushInstanceExchange") DirectExchange wsPushInstanceExchange) {
        return BindingBuilder.bind(messagePushInstanceQueue).to(wsPushInstanceExchange)
                .with(instanceIdProvider.getInstanceId());
    }

    @Bean
    public Binding messagePushRetryBinding(@Qualifier("messagePushRetryQueue") Queue messagePushRetryQueue,
                                           InstanceIdProvider instanceIdProvider,
                                           @Qualifier("wsPushRetryExchange") DirectExchange wsPushRetryExchange) {
        return BindingBuilder.bind(messagePushRetryQueue).to(wsPushRetryExchange)
                .with(instanceIdProvider.getInstanceId());
    }

    @Bean
    public Binding wsPushDeadLetterBinding(@Qualifier("wsPushDeadLetterQueue") Queue wsPushDeadLetterQueue,
                                           @Qualifier("wsPushDeadLetterExchange") DirectExchange wsPushDeadLetterExchange) {
        return BindingBuilder.bind(wsPushDeadLetterQueue).to(wsPushDeadLetterExchange)
                .with(RabbitMQConfig.WS_PUSH_DEAD_LETTER_ROUTING_KEY);
    }

}
