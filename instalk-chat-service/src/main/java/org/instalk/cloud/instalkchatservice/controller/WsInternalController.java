package org.instalk.cloud.instalkchatservice.controller;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastGroupDeleteDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastRevokeDTO;
import org.instalk.cloud.common.model.dto.internal.WsDeleteFriendDTO;
import org.instalk.cloud.common.model.dto.internal.WsRevokeMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsSendPrivateMessageDTO;
import org.instalk.cloud.instalkchatservice.mq.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ws")
public class WsInternalController implements WebSocketAPI {

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void sendFriendDeletedNotification(@RequestBody WsDeleteFriendDTO wsDeleteFriendDTO) {
        messageProducer.publishFriendDeleted(wsDeleteFriendDTO);
    }

    @Override
    public void sendMessageToUser(@RequestBody WsSendPrivateMessageDTO wsSendPrivateMessageDTO) {
        messageProducer.publishSendPrivateMessage(wsSendPrivateMessageDTO);
    }

    @Override
    public void broadcastMessageToUsers(@RequestBody WsBroadcastMessageDTO wsBroadcastMessageDTO) {
        messageProducer.publishBroadcastMessage(wsBroadcastMessageDTO);
    }

    @Override
    public void sendMessageRecallNotification(@RequestBody WsRevokeMessageDTO wsRevokeMessageDTO) {
        messageProducer.publishMessageRecall(wsRevokeMessageDTO);
    }

    @Override
    public void broadcastMessageRecallNotification(@RequestBody WsBroadcastRevokeDTO wsBroadcastRevokeDTO) {
        messageProducer.publishBroadcastRecall(wsBroadcastRevokeDTO);
    }

    @Override
    public void broadcastGroupDeletedNotification(@RequestBody WsBroadcastGroupDeleteDTO wsBroadcastGroupDeleteDTO) {
        messageProducer.publishGroupDeleted(wsBroadcastGroupDeleteDTO);
    }
}
