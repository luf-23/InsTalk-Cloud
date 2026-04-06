package org.instalk.cloud.instalkchatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.vo.MessageVO;
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

    private static final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public WebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineUsers.put(userId, session);
            log.info("用户 {} 已连接 WebSocket，当前在线用户数：{}", userId, onlineUsers.size());
            broadcastUserOnlineStatus(userId, true);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.debug("收到用户 {} 的消息：{}", userId, message.getPayload());
        if ("PING".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("PONG"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("用户 {} 已断开 WebSocket，当前在线用户数：{}", userId, onlineUsers.size());
            broadcastUserOnlineStatus(userId, false);
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

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        return null;
    }

    public void sendMessageToUser(Long userId, MessageVO messageVO) {
        WebSocketSession session = onlineUsers.get(userId);
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
            log.debug("用户 {} 不在线，无法发送消息", userId);
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
            for (Map.Entry<Long, WebSocketSession> entry : onlineUsers.entrySet()) {
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
        WebSocketSession session = onlineUsers.get(userId);
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
        } else {
            log.debug("用户 {} 不在线，无法发送消息撤回通知", userId);
        }
    }

    public void broadcastMessageRecallNotification(Iterable<Long> userIds, Long messageId) {
        for (Long userId : userIds) {
            sendMessageRecallNotification(userId, messageId);
        }
    }

    public void sendFriendDeletedNotification(Long userId, Long deleterId) {
        WebSocketSession session = onlineUsers.get(userId);
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
        } else {
            log.debug("用户 {} 不在线，无法发送好友删除通知", userId);
        }
    }

    public void sendGroupDeletedNotification(Long userId, Long groupId) {
        WebSocketSession session = onlineUsers.get(userId);
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
        } else {
            log.debug("用户 {} 不在线，无法发送群组解散通知", userId);
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

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = onlineUsers.get(userId);
        return session != null && session.isOpen();
    }

    public Map<Long, Boolean> getOnlineUsers() {
        Map<Long, Boolean> result = new ConcurrentHashMap<>();
        for (Long userId : onlineUsers.keySet()) {
            result.put(userId, true);
        }
        return result;
    }
}
