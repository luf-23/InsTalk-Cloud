package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.MakeFriendsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface FriendshipAPI {

    @PostMapping("/makeFriendsWithRobot")
    void makeFriendsWithRobot(@RequestBody MakeFriendsDTO makeFriendsDTO);
}
