package org.instalk.cloud.instalkwebsocketservice.controller;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.instalk.cloud.common.model.dto.internal.WebSocketDeleteFriendDTO;
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
    public void sendFriendDeletedNotification(@RequestBody WebSocketDeleteFriendDTO webSocketDeleteFriendDTO) {
        webSocketHandler.sendFriendDeletedNotification(webSocketDeleteFriendDTO.getId(),webSocketDeleteFriendDTO.getMyId());
    }
}
