package org.instalk.cloud.instalkfriendshipservice.controller;

import org.instalk.cloud.common.feign.api.FriendshipAPI;
import org.instalk.cloud.instalkfriendshipservice.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/friendship")
public class InternalController implements FriendshipAPI {

    @Autowired
    private FriendshipService friendshipService;

    @Override
    public void makeFriendsWithRobot(@RequestParam("minId") Long minId, @RequestParam("maxId") Long maxId) {
        friendshipService.makeFriendsWithRobot(minId, maxId);
    }
}
