package org.instalk.cloud.common.feign.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface FriendshipAPI {

    @GetMapping("/makeFriendsWithRobot")
    void makeFriendsWithRobot(@RequestParam("minId") Long minId, @RequestParam("maxId") Long maxId);
}
