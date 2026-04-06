package org.instalk.cloud.instalksocialservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
@MapperScan(basePackages = {
    "org.instalk.cloud.instalkfriendshipservice.mapper",
    "org.instalk.cloud.instalkgroupservice.mapper"
})
@ComponentScan(basePackages = {
    "org.instalk.cloud.instalkfriendshipservice",
    "org.instalk.cloud.instalkgroupservice"
})
public class InstalkSocialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkSocialServiceApplication.class, args);
    }
}
