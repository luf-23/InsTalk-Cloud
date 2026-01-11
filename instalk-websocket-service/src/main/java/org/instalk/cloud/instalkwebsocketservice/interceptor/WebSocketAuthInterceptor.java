package org.instalk.cloud.instalkwebsocketservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //attributes为当前会话的所有属性

        // 从查询参数中获取token
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = extractToken(query);
            if (token != null) {
                try {
                    // 验证token并解析用户信息
                    Map<String, Object> claims = JwtUtil.parseToken(token);
                    Long userId = ((Number) claims.get("id")).longValue();

                    // 将用户ID存储到WebSocket会话属性中
                    attributes.put("userId", userId);
                    log.info("WebSocket 握手成功，用户ID：{}", userId);
                    return true;
                } catch (Exception e) {
                    log.error("WebSocket 认证失败：{}", e.getMessage());
                    return false;
                }
            }
        }

        log.warn("WebSocket 握手失败：缺少token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手后异常：{}", exception.getMessage());
        }
    }

    /**
     * 从查询字符串中提取token
     */
    private String extractToken(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6); // "token=".length() = 6
            }
        }
        return null;
    }
}
