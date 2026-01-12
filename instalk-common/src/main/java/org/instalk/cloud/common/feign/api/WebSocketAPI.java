package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.*;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface WebSocketAPI {

    @PostMapping("/deleteFriend")
    void sendFriendDeletedNotification(@RequestBody WsDeleteFriendDTO wsDeleteFriendDTO);

    @PostMapping("/sendPrivateMessage")
    void sendMessageToUser(@RequestBody WsSendPrivateMessageDTO wsSendPrivateMessageDTO);

    @PostMapping("/broadcastMessage")
    void broadcastMessageToUsers(@RequestBody WsBroadcastMessageDTO wsBroadcastMessageDTO);

    @PostMapping("/revokeMessage")
    void sendMessageRecallNotification(@RequestBody WsRevokeMessageDTO wsRevokeMessageDTO);

    @PostMapping("/broadcastRevoke")
    void broadcastMessageRecallNotification(@RequestBody WsBroadcastRevokeDTO wsBroadcastRevokeDTO);

    @PostMapping("/broadcastGroupDelete")
    void broadcastGroupDeletedNotification(@RequestBody WsBroadcastGroupDeleteDTO wsBroadcastGroupDeleteDTO);
}
