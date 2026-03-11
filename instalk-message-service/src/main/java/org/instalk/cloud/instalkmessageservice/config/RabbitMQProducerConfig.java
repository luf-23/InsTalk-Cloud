package org.instalk.cloud.instalkmessageservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQProducerConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true);
        
        template.setReturnsCallback(returned -> 
            log.error("消息路由失败: 交换机={}, 路由键={}", returned.getExchange(), returned.getRoutingKey())
        );
        
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息投递失败: {}", cause);
            }
        });
        
        return template;
    }
}
