package org.instalk.cloud.instalkaiservice.service;

import org.instalk.cloud.instalkaiservice.mapper.UserAiConfigMapper;
import org.springframework.stereotype.Service;

@Service
public class UserAiConfigService {

    private final UserAiConfigMapper userAiConfigMapper;

    public UserAiConfigService(UserAiConfigMapper userAiConfigMapper) {
        this.userAiConfigMapper = userAiConfigMapper;
    }

    public void add(Long userId, Long robotId) {
        userAiConfigMapper.add(userId, robotId);
    }
}
