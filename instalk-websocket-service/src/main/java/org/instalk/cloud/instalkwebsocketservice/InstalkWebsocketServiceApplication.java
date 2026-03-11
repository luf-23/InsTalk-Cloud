package org.instalk.cloud.instalkwebsocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"org.instalk.cloud.instalkwebsocketservice",
	"org.instalk.cloud.infrastructure.rabbitmq"
})
public class InstalkWebsocketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstalkWebsocketServiceApplication.class, args);
	}

}
