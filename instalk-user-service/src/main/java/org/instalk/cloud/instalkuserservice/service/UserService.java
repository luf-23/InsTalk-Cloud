package org.instalk.cloud.instalkuserservice.service;

import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.instalkuserservice.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public User add(User user) {
        userMapper.add(user);
        return user;
    }

    public User addRobot(User robot) {
        userMapper.addRobot(robot);
        return robot;
    }
}
