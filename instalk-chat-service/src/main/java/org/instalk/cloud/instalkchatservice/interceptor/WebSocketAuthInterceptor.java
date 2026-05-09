package org.instalk.cloud.instalkchatservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = extractToken(query);
            if (token != null) {
                try {
                    Map<String, Object> claims = JwtUtil.parseToken(token);
                    Long userId = ((Number) claims.get("id")).longValue();
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

    private String extractToken(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                String raw = param.substring(6);
                try {
                    return URLDecoder.decode(raw, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return raw;
                }
            }
        }
        return null;
    }
}
