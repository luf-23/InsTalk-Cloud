package org.instalk.cloud.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.instalk.cloud.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 提供常用的Redis操作
 */
@Component
public class RedisUtilImpl implements RedisUtil {

    @Getter
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public RedisUtilImpl() {
        this.objectMapper = new ObjectMapper();
        // 支持 Java 8 日期时间类型
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    @Override
    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis缓存对象序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonValue, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis缓存对象反序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void putHashValue(String key, String hashKey, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
        stringRedisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Map<String, String> getHashValues(String key) {
        Map<Object, Object> values = stringRedisTemplate.opsForHash().entries(key);
        Map<String, String> result = new HashMap<>();
        values.forEach((hashKey, value) -> result.put(String.valueOf(hashKey), String.valueOf(value)));
        return result;
    }

    @Override
    public void removeHashValue(String key, String hashKey) {
        stringRedisTemplate.opsForHash().delete(key, hashKey);
    }

}
