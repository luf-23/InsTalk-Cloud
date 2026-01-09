package org.instalk.cloud.instalkaiconfigservice.service;

import org.instalk.cloud.instalkaiconfigservice.mapper.UserAiConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAiConfigService {

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    public void add(Long userId, Long robotId) {
        userAiConfigMapper.add(userId, robotId);
    }
}
