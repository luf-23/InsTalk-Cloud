package org.instalk.cloud.instalkidentityservice.config;

import org.instalk.cloud.common.interceptor.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置
 * 配置Feign请求拦截器，在服务调用时自动添加用户信息到请求头
 */
@Configuration
public class FeignConfig {

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
}
