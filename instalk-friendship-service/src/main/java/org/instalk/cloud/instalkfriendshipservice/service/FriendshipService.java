package org.instalk.cloud.instalkfriendshipservice.service;

import org.instalk.cloud.instalkfriendshipservice.mapper.FriendshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipMapper friendshipMapper;

    public void makeFriendsWithRobot(Long minId, Long maxId) {
        friendshipMapper.makeFriendsWithRobot(minId,maxId);
    }
}
