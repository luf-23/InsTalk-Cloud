package org.instalk.cloud.instalkchatservice.controller;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastGroupDeleteDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastRevokeDTO;
import org.instalk.cloud.common.model.dto.internal.WsDeleteFriendDTO;
import org.instalk.cloud.common.model.dto.internal.WsRevokeMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsSendPrivateMessageDTO;
import org.instalk.cloud.instalkchatservice.service.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ws")
public class WsInternalController implements WebSocketAPI {

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
    public void broadcastMessageToUsers(@RequestBody WsBroadcastMessageDTO wsBroadcastMessageDTO) {
        webSocketHandler.broadcastMessageToUsers(wsBroadcastMessageDTO.getReceiverIds(), wsBroadcastMessageDTO.getMessageVO());
    }

    @Override
    public void sendMessageRecallNotification(@RequestBody WsRevokeMessageDTO wsRevokeMessageDTO) {
        webSocketHandler.sendMessageRecallNotification(wsRevokeMessageDTO.getReceiverId(), wsRevokeMessageDTO.getMessageId());
    }

    @Override
    public void broadcastMessageRecallNotification(@RequestBody WsBroadcastRevokeDTO wsBroadcastRevokeDTO) {
        webSocketHandler.broadcastMessageRecallNotification(wsBroadcastRevokeDTO.getReceiverIds(), wsBroadcastRevokeDTO.getMessageId());
    }

    @Override
    public void broadcastGroupDeletedNotification(@RequestBody WsBroadcastGroupDeleteDTO wsBroadcastGroupDeleteDTO) {
        webSocketHandler.broadcastGroupDeletedNotification(wsBroadcastGroupDeleteDTO.getMemberIds(), wsBroadcastGroupDeleteDTO.getGroupId());
    }
}
