package org.instalk.cloud.common.interceptor;

import org.instalk.cloud.common.util.RequestContextUtil;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取用户信息
        Long userId = RequestContextUtil.getUserId();
        String username = RequestContextUtil.getUsername();
        
        // 如果存在用户信息，则存入ThreadLocal
        if (userId != null || username != null) {
            Map<String, Object> claims = new HashMap<>();
            if (userId != null) {
                claims.put("id", userId);
            }
            if (username != null) {
                claims.put("username", username);
            }
            String jti = RequestContextUtil.getTokenJti();
            if (jti != null) {
                claims.put("jti", jti);
            }
            
            ThreadLocalUtil.set(claims);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                 Object handler, Exception ex) {
        // 请求完成后清除ThreadLocal，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}
