package org.instalk.cloud.instalkaiservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "org.instalk.cloud.instalkaiservice")
@MapperScan("org.instalk.cloud.instalkaiservice.mapper")
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
public class InstalkAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkAiServiceApplication.class, args);
    }
}
