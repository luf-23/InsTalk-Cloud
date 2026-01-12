package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.FriendshipDTO;
import org.instalk.cloud.common.model.po.Friendship;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface FriendshipAPI {

    @PostMapping("/makeFriendsWithRobot")
    void makeFriendsWithRobot(@RequestBody FriendshipDTO friendshipDTO);

    @PostMapping("/getFriendship")
    Friendship getByUserId1AndUserId2(@RequestBody FriendshipDTO friendshipDTO);
}
