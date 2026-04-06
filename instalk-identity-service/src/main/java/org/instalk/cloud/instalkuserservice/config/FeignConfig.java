package org.instalk.cloud.instalkuserservice.config;

import org.instalk.cloud.common.interceptor.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置
 * 配置Feign拦截器，在服务间调用时自动传递用户信息请求头
 */
@Configuration
public class FeignConfig {

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
}
