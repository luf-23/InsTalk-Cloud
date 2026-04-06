package org.instalk.cloud.instalkidentityservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
@MapperScan(basePackages = "org.instalk.cloud.instalkuserservice.mapper")
@ComponentScan(basePackages = {
    "org.instalk.cloud.instalkuserservice",
    "org.instalk.cloud.instalkauthservice",
    "org.instalk.cloud.instalkossservice"
})
public class InstalkIdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkIdentityServiceApplication.class, args);
    }
}