package org.instalk.cloud.instalkauthservice;

import org.instalk.cloud.common.util.TokenUtil;
import org.instalk.cloud.infrastructure.redis.TokenUtilImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"org.instalk.cloud.common.feign.client"})
public class InstalkAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkAuthServiceApplication.class, args);
    }

}
