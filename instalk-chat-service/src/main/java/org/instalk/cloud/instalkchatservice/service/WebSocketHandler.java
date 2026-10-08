package org.instalk.cloud.instalkchatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, Map<String, WebSocketSession>> localSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

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
            String clientId = getClientIdFromSession(session);
            Map<String, WebSocketSession> sessions = localSessions.computeIfAbsent(
                    userId, ignored -> new ConcurrentHashMap<>());
            WebSocketSession previousSession = sessions.put(clientId, session);
            if (previousSession != null && previousSession != session && previousSession.isOpen()) {
                try {
                    previousSession.close(CloseStatus.NORMAL);
                } catch (IOException exception) {
                    log.debug("关闭用户 {} 的旧 WebSocket 连接失败", userId, exception);
                }
            }
            wsOnlineRegistry.markOnline(userId);
            log.info("用户 {} 已连接 WebSocket，本实例在线用户数：{}", userId, localSessions.size());
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
            Map<String, WebSocketSession> sessions = localSessions.get(userId);
            if (sessions != null) {
                sessions.remove(getClientIdFromSession(session), session);
                if (sessions.isEmpty()) {
                    localSessions.remove(userId, sessions);
                    wsOnlineRegistry.markOffline(userId);
                }
            }
            log.info("用户 {} 已断开 WebSocket，本实例在线用户数：{}", userId, localSessions.size());
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

    private String getClientIdFromSession(WebSocketSession session) {
        Object clientIdAttr = session.getAttributes().get("clientId");
        return clientIdAttr instanceof String ? (String) clientIdAttr : session.getId();
    }

    public void sendMessageToUser(Long userId, MessageVO messageVO) {
        sendPayloadToUser(userId, "NEW_MESSAGE", messageVO);
    }

    public void broadcastMessageToUsers(Iterable<Long> userIds, MessageVO messageVO) {
        for (Long userId : userIds) {
            sendMessageToUser(userId, messageVO);
        }
    }

    public void sendMessageRecallNotification(Long userId, Long messageId) {
        sendPayloadToUser(userId, "MESSAGE_RECALL", Map.of("messageId", messageId));
    }

    public void broadcastMessageRecallNotification(Iterable<Long> userIds, Long messageId) {
        for (Long userId : userIds) {
            sendMessageRecallNotification(userId, messageId);
        }
    }

    public void sendFriendDeletedNotification(Long userId, Long deleterId) {
        sendPayloadToUser(userId, "FRIEND_DELETED", Map.of("friendId", deleterId));
    }

    public void sendGroupDeletedNotification(Long userId, Long groupId) {
        sendPayloadToUser(userId, "GROUP_DELETED", Map.of("groupId", groupId));
    }

    private void sendPayloadToUser(Long userId, String type, Object data) {
        Map<String, WebSocketSession> sessions = localSessions.get(userId);
        if (sessions == null) {
            log.debug("用户 {} 不在本实例，无法发送消息", userId);
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "data", data
            ));
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession session = entry.getValue();
                if (!session.isOpen()) {
                    sessions.remove(entry.getKey(), session);
                    continue;
                }
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException exception) {
                    sessions.remove(entry.getKey(), session);
                    log.warn("发送 WebSocket 消息失败，用户={}, clientId={}", userId, entry.getKey(), exception);
                }
            }
            log.debug("发送 WebSocket 消息给用户 {}, type={}", userId, type);
        } catch (IOException exception) {
            log.error("创建 WebSocket 消息失败，用户={}, type={}", userId, type, exception);
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
        Map<String, WebSocketSession> sessions = localSessions.get(userId);
        return sessions != null && sessions.values().stream().anyMatch(WebSocketSession::isOpen);
    }

    public boolean isUserOnline(Long userId) {
        return wsOnlineRegistry.isOnline(userId);
    }

    public Map<Long, Boolean> getOnlineStatuses(List<Long> userIds) {
        Map<Long, Boolean> statuses = new LinkedHashMap<>();
        if (userIds == null) {
            return statuses;
        }
        for (Long userId : userIds) {
            if (userId != null && !statuses.containsKey(userId)) {
                statuses.put(userId, isUserOnline(userId));
            }
        }
        return statuses;
    }
}
