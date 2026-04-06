package org.instalk.cloud.instalkuserservice.config;

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

    /**
     * 创建用户上下文拦截器Bean
     */
    @Bean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor())
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/error", "/actuator/**");  // 排除错误页和监控端点
    }
}
