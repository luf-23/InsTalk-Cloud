package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.WebSocketDeleteFriendDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface WebSocketAPI {

    @PostMapping("/deleteFriend")
    void sendFriendDeletedNotification(@RequestBody WebSocketDeleteFriendDTO webSocketDeleteFriendDTO);
}
