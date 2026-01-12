package org.instalk.cloud.instalkfriendshipservice.controller;

import org.instalk.cloud.common.feign.api.FriendshipAPI;
import org.instalk.cloud.common.model.dto.internal.FriendshipDTO;
import org.instalk.cloud.common.model.po.Friendship;
import org.instalk.cloud.instalkfriendshipservice.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/friendship")
public class InternalController implements FriendshipAPI {

    @Autowired
    private FriendshipService friendshipService;

    @Override
    public void makeFriendsWithRobot(@RequestBody FriendshipDTO friendshipDTO) {
        friendshipService.makeFriendsWithRobot(friendshipDTO.getMinId(), friendshipDTO.getMaxId());
    }

    @Override
    public Friendship getByUserId1AndUserId2(@RequestBody FriendshipDTO friendshipDTO) {
        return friendshipService.getByUserId1AndUserId2(friendshipDTO.getMinId(), friendshipDTO.getMaxId());
    }
}
