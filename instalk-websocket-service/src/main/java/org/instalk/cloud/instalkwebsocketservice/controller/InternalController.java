package org.instalk.cloud.instalkwebsocketservice.controller;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.instalk.cloud.common.model.dto.internal.*;
import org.instalk.cloud.instalkwebsocketservice.service.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ws")
public class InternalController implements WebSocketAPI {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public void sendFriendDeletedNotification(@RequestBody WsDeleteFriendDTO wsDeleteFriendDTO) {
        webSocketHandler.sendFriendDeletedNotification(wsDeleteFriendDTO.getId(), wsDeleteFriendDTO.getMyId());
    }

    @Override
    public void sendMessageToUser(@RequestBody WsSendPrivateMessageDTO wsSendPrivateMessageDTO) {
        webSocketHandler.sendMessageToUser(wsSendPrivateMessageDTO.getReceiverId(), wsSendPrivateMessageDTO.getMessageVO());
    }

    @Override
    public void broadcastMessageToUsers(WsBroadcastMessageDTO wsBroadcastMessageDTO) {
        webSocketHandler.broadcastMessageToUsers(wsBroadcastMessageDTO.getReceiverIds(), wsBroadcastMessageDTO.getMessageVO());
    }

    @Override
    public void sendMessageRecallNotification(WsRevokeMessageDTO wsRevokeMessageDTO) {
        webSocketHandler.sendMessageRecallNotification(wsRevokeMessageDTO.getReceiverId(), wsRevokeMessageDTO.getMessageId());
    }

    @Override
    public void broadcastMessageRecallNotification(WsBroadcastRevokeDTO wsBroadcastRevokeDTO) {
        webSocketHandler.broadcastMessageRecallNotification(wsBroadcastRevokeDTO.getReceiverIds(), wsBroadcastRevokeDTO.getMessageId());
    }

    @Override
    public void broadcastGroupDeletedNotification(WsBroadcastGroupDeleteDTO wsBroadcastGroupDeleteDTO) {
        webSocketHandler.broadcastGroupDeletedNotification(wsBroadcastGroupDeleteDTO.getMemberIds(), wsBroadcastGroupDeleteDTO.getGroupId());
    }
}
