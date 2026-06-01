package org.instalk.cloud.instalkchatservice.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 每个 chat-service 实例绑定独立 Fanout 队列，广播 WebSocket 推送到所有实例。
 */
@Configuration
public class MessageRabbitMQConfig {

    @Bean
    public Queue messagePushInstanceQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding messagePushInstanceBinding(@Qualifier("messagePushInstanceQueue") Queue messagePushInstanceQueue,
                                              @Qualifier("wsPushFanoutExchange") FanoutExchange wsPushFanoutExchange) {
        return BindingBuilder.bind(messagePushInstanceQueue).to(wsPushFanoutExchange);
    }
}
