package org.instalk.cloud.instalkchatservice.config;

import org.instalk.cloud.instalkchatservice.interceptor.WebSocketAuthInterceptor;
import org.instalk.cloud.instalkchatservice.service.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 浏览器页面与网关/服务往往不同源（如 Vite :5173 → 网关 :10010），不放开 Origin 时握手会被拒绝为 403
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
