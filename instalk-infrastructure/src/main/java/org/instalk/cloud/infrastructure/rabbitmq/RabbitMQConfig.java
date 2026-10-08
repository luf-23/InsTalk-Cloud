package org.instalk.cloud.infrastructure.rabbitmq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
public class RabbitMQConfig {

    /** WebSocket 按实例定向投递交换机 */
    public static final String WS_PUSH_INSTANCE_EXCHANGE = "instalk.ws.push.instance.exchange";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
