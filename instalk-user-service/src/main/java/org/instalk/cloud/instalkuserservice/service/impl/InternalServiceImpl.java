package org.instalk.cloud.instalkuserservice.service.impl;

import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.instalkuserservice.mapper.UserMapper;
import org.instalk.cloud.instalkuserservice.service.InternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternalServiceImpl implements InternalService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
}
