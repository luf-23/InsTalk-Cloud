package org.instalk.cloud.infrastructure.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
public class RabbitMQConfig {

    // 队列名称
    public static final String PRIVATE_MESSAGE_QUEUE = "instalk.message.private";
    public static final String GROUP_MESSAGE_QUEUE = "instalk.message.group";
    public static final String DEAD_LETTER_QUEUE = "instalk.message.dlq";

    // 交换机名称
    public static final String MESSAGE_EXCHANGE = "instalk.message.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "instalk.message.dlx";
    /** WebSocket 推送 Fanout 交换机：每个 chat-service 实例绑定独立队列，广播到所有实例 */
    public static final String WS_PUSH_FANOUT_EXCHANGE = "instalk.ws.push.exchange";

    // 路由键
    public static final String PRIVATE_MESSAGE_ROUTING_KEY = "message.private";
    public static final String GROUP_MESSAGE_ROUTING_KEY = "message.group";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dead");
    }

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange(MESSAGE_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange wsPushFanoutExchange() {
        return new FanoutExchange(WS_PUSH_FANOUT_EXCHANGE, true, false);
    }

    @Bean
    public Queue privateMessageQueue() {
        return QueueBuilder.durable(PRIVATE_MESSAGE_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("dead")
                .ttl(1800000)
                .build();
    }

    @Bean
    public Binding privateMessageBinding() {
        return BindingBuilder.bind(privateMessageQueue()).to(messageExchange()).with(PRIVATE_MESSAGE_ROUTING_KEY);
    }

    @Bean
    public Queue groupMessageQueue() {
        return QueueBuilder.durable(GROUP_MESSAGE_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("dead")
                .ttl(1800000)
                .build();
    }

    @Bean
    public Binding groupMessageBinding() {
        return BindingBuilder.bind(groupMessageQueue()).to(messageExchange()).with(GROUP_MESSAGE_ROUTING_KEY);
    }
}
