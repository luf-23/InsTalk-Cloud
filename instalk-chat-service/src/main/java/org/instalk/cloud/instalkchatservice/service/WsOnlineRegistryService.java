package org.instalk.cloud.instalkchatservice.service;

import lombok.extern.slf4j.Slf4j;
import org.instalk.cloud.common.util.RedisUtil;
import org.instalk.cloud.instalkchatservice.config.InstanceIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WsOnlineRegistryService {

    private static final String KEY_PREFIX = "ws:online:";
    /** 前端心跳 15s，TTL 设为 3 倍并留余量 */
    private static final long ONLINE_TTL_SECONDS = 90;
    private static final String ONLINE_KEY_PREFIX = "ws:online:";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private InstanceIdProvider instanceIdProvider;

    public void markOnline(Long userId) {
        redisUtil.putHashValue(buildKey(userId), instanceIdProvider.getInstanceId(),
                String.valueOf(System.currentTimeMillis()), ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("用户 {} 已写入 Redis 在线状态, TTL={}s", userId, ONLINE_TTL_SECONDS);
    }

    public void refreshOnline(Long userId) {
        markOnline(userId);
    }

    /**
     * 仅当 Redis 中记录的是本实例时删除，避免用户已重连到其他实例后误删。
     */
    public void markOffline(Long userId) {
        redisUtil.removeHashValue(buildKey(userId), instanceIdProvider.getInstanceId());
        log.debug("用户 {} 已从 Redis 移除本实例在线状态", userId);
    }

    public boolean isOnline(Long userId) {
        return !findInstanceIds(userId).isEmpty();
    }

    public List<String> findInstanceIds(Long userId) {
        long now = System.currentTimeMillis();
        List<String> instanceIds = new ArrayList<>();
        for (Map.Entry<String, String> entry : redisUtil.getHashValues(buildKey(userId)).entrySet()) {
            try {
                if (now - Long.parseLong(entry.getValue()) <= ONLINE_TTL_SECONDS * 1000) {
                    instanceIds.add(entry.getKey());
                } else {
                    redisUtil.removeHashValue(buildKey(userId), entry.getKey());
                }
            } catch (NumberFormatException exception) {
                redisUtil.removeHashValue(buildKey(userId), entry.getKey());
            }
        }
        return instanceIds;
    }

    private String buildKey(Long userId) {
        return ONLINE_KEY_PREFIX + userId;
    }
}
