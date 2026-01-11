package org.instalk.cloud.instalkfriendshipservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
public class InstalkFriendshipServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkFriendshipServiceApplication.class, args);
    }

}
