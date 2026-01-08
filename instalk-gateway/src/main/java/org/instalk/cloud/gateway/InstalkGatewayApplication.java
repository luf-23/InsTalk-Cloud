package org.instalk.cloud.gateway;

import org.instalk.cloud.infrastructure.redis.RedisUtilImpl;
import org.instalk.cloud.infrastructure.redis.TokenUtilImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {RedisUtilImpl.class, TokenUtilImpl.class},scanBasePackages ="org.instalk.cloud.gateway")
public class InstalkGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstalkGatewayApplication.class, args);
    }
}
