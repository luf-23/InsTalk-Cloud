package org.instalk.cloud.instalkmessageservice.config;

import org.instalk.cloud.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册UserContextInterceptor拦截器，用于从请求头中提取用户信息
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/actuator/**");
    }
}
