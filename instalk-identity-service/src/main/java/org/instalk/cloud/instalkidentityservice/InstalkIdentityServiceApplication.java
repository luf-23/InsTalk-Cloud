package org.instalk.cloud.instalkidentityservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
@MapperScan("org.instalk.cloud.instalkidentityservice.mapper")
public class InstalkIdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkIdentityServiceApplication.class, args);
    }
}