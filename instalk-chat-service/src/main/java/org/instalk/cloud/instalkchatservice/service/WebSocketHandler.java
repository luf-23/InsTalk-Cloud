package org.instalk.cloud.instalkchatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.instalk.cloud.instalkchatservice.mq.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> localSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private WsOnlineRegistryService wsOnlineRegistry;

    public WebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            localSessions.put(userId, session);
            wsOnlineRegistry.markOnline(userId);
            log.info("用户 {} 已连接 WebSocket，本实例在线用户数：{}", userId, localSessions.size());
            messageProducer.publishOnlineStatus(userId, true);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.debug("收到用户 {} 的消息：{}", userId, message.getPayload());
        if ("PING".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("PONG"));
            if (userId != null) {
                wsOnlineRegistry.refreshOnline(userId);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            localSessions.remove(userId);
            wsOnlineRegistry.markOffline(userId);
            log.info("用户 {} 已断开 WebSocket，本实例在线用户数：{}", userId, localSessions.size());
            messageProducer.publishOnlineStatus(userId, false);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("用户 {} WebSocket 传输错误：{}", userId, exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    /** Fanout 消费端调用：向本实例 WebSocket 客户端广播上下线通知 */
    public void broadcastOnlineStatusChange(Long userId, boolean online) {
        broadcastUserOnlineStatus(userId, online);
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        return null;
    }

    public void sendMessageToUser(Long userId, MessageVO messageVO) {
        WebSocketSession session = localSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> payload = Map.of(
                        "type", "NEW_MESSAGE",
                        "data", messageVO
                );
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
                log.debug("发送消息给用户 {}", userId);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败：{}", userId, e.getMessage());
            }
        } else {
            log.debug("用户 {} 不在本实例，无法发送消息", userId);
        }
    }

    public void broadcastMessageToUsers(Iterable<Long> userIds, MessageVO messageVO) {
        for (Long userId : userIds) {
            sendMessageToUser(userId, messageVO);
        }
    }

    private void broadcastUserOnlineStatus(Long userId, boolean online) {
        Map<String, Object> payload = Map.of(
                "type", "USER_ONLINE_STATUS",
                "data", Map.of(
                        "userId", userId,
                        "online", online
                )
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (Map.Entry<Long, WebSocketSession> entry : localSessions.entrySet()) {
                if (entry.getKey().equals(userId) && online) {
                    continue;
                }
                WebSocketSession session = entry.getValue();
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("广播在线状态失败：{}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("创建在线状态消息失败：{}", e.getMessage());
        }
    }

    public void sendMessageRecallNotification(Long userId, Long messageId) {
        WebSocketSession session = localSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> payload = Map.of(
                        "type", "MESSAGE_RECALL",
                        "data", Map.of("messageId", messageId)
                );
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
                log.debug("发送消息撤回通知给用户 {}，消息ID：{}", userId, messageId);
            } catch (IOException e) {
                log.error("发送消息撤回通知给用户 {} 失败：{}", userId, e.getMessage());
            }
        }
    }

    public void broadcastMessageRecallNotification(Iterable<Long> userIds, Long messageId) {
        for (Long userId : userIds) {
            sendMessageRecallNotification(userId, messageId);
        }
    }

    public void sendFriendDeletedNotification(Long userId, Long deleterId) {
        WebSocketSession session = localSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> payload = Map.of(
                        "type", "FRIEND_DELETED",
                        "data", Map.of("friendId", deleterId)
                );
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
                log.info("发送好友删除通知给用户 {}，删除者ID：{}", userId, deleterId);
            } catch (IOException e) {
                log.error("发送好友删除通知给用户 {} 失败：{}", userId, e.getMessage());
            }
        }
    }

    public void sendGroupDeletedNotification(Long userId, Long groupId) {
        WebSocketSession session = localSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> payload = Map.of(
                        "type", "GROUP_DELETED",
                        "data", Map.of("groupId", groupId)
                );
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
                log.info("发送群组解散通知给用户 {}，群组ID：{}", userId, groupId);
            } catch (IOException e) {
                log.error("发送群组解散通知给用户 {} 失败：{}", userId, e.getMessage());
            }
        }
    }

    public void broadcastGroupDeletedNotification(Iterable<Long> userIds, Long groupId) {
        int count = 0;
        for (Long userId : userIds) {
            sendGroupDeletedNotification(userId, groupId);
            count++;
        }
        log.info("广播群组解散通知，群组ID：{}，通知用户数：{}", groupId, count);
    }

    public boolean hasLocalSession(Long userId) {
        WebSocketSession session = localSessions.get(userId);
        return session != null && session.isOpen();
    }

    public boolean isUserOnline(Long userId) {
        return wsOnlineRegistry.isOnline(userId);
    }
}
