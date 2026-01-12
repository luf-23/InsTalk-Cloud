package org.instalk.cloud.instalkgroupservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.instalk.cloud.common.feign.client")
public class InstalkGroupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstalkGroupServiceApplication.class, args);
    }

}
