package org.instalk.cloud.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign请求拦截器
 * 在服务间调用时自动传递用户信息的请求头
 * 
 * 使用方法：在各个微服务中配置此拦截器
 * 
 * @Configuration
 * public class FeignConfig {
 *     @Bean
 *     public FeignRequestInterceptor feignRequestInterceptor() {
 *         return new FeignRequestInterceptor();
 *     }
 * }
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_TOKEN_JTI = "X-Token-Jti";
    private static final String HEADER_CLAIM_PREFIX = "X-Claim-";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 传递用户ID
            String userId = request.getHeader(HEADER_USER_ID);
            if (userId != null && !userId.isEmpty()) {
                template.header(HEADER_USER_ID, userId);
            }
            
            // 传递用户名
            String username = request.getHeader(HEADER_USERNAME);
            if (username != null && !username.isEmpty()) {
                template.header(HEADER_USERNAME, username);
            }
            
            // 传递Token JTI
            String jti = request.getHeader(HEADER_TOKEN_JTI);
            if (jti != null && !jti.isEmpty()) {
                template.header(HEADER_TOKEN_JTI, jti);
            }
            
            // 传递所有以X-Claim-开头的请求头
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (headerName.startsWith(HEADER_CLAIM_PREFIX)) {
                    String headerValue = request.getHeader(headerName);
                    if (headerValue != null && !headerValue.isEmpty()) {
                        template.header(headerName, headerValue);
                    }
                }
            });
        }
    }
}
