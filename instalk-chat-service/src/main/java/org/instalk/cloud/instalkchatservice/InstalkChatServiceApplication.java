package org.instalk.cloud.instalkchatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
@ComponentScan(basePackages = {
    "org.instalk.cloud.instalkchatservice",
    "org.instalk.cloud.infrastructure.rabbitmq"
})
public class InstalkChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkChatServiceApplication.class, args);
    }

}
