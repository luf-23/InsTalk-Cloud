package org.instalk.cloud.instalkchatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.instalk.cloud.infrastructure.rabbitmq.RabbitMQConfig;

/** WebSocket 按实例定向投递队列。 */
@Configuration
public class MessageRabbitMQConfig {

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
