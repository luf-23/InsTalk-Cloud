package org.instalk.cloud.instalkchatservice.service;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.util.RedisUtil;
import org.instalk.cloud.instalkchatservice.config.InstanceIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WsOnlineRegistryService {

    private static final String KEY_PREFIX = "ws:online:";
    /** 前端心跳 15s，TTL 设为 3 倍并留余量 */
    private static final long ONLINE_TTL_SECONDS = 90;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private InstanceIdProvider instanceIdProvider;

    public void markOnline(Long userId) {
        redisUtil.set(buildKey(userId), instanceIdProvider.getInstanceId(), ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("用户 {} 已写入 Redis 在线状态, TTL={}s", userId, ONLINE_TTL_SECONDS);
    }

    public void refreshOnline(Long userId) {
        String key = buildKey(userId);
        if (Boolean.TRUE.equals(redisUtil.hasKey(key))) {
            redisUtil.set(key, instanceIdProvider.getInstanceId(), ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        } else {
            markOnline(userId);
        }
    }

    /**
     * 仅当 Redis 中记录的是本实例时删除，避免用户已重连到其他实例后误删。
     */
    public void markOffline(Long userId) {
        String key = buildKey(userId);
        String holder = redisUtil.get(key);
        if (holder != null && holder.equals(instanceIdProvider.getInstanceId())) {
            redisUtil.delete(key);
            log.debug("用户 {} 已从 Redis 移除在线状态", userId);
        }
    }

    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(redisUtil.hasKey(buildKey(userId)));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
