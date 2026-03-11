package org.instalk.cloud.common.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface RedisUtil {
    void set(String key, String value, long timeout, TimeUnit unit);

    String get(String key);

    Boolean hasKey(String key);

    Boolean delete(String key);

    Long delete(Collection<String> keys);

    /**
     * 缓存对象（自动序列化为JSON）
     * @param key 缓存键
     * @param value 对象值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    <T> void setObject(String key, T value, long timeout, TimeUnit unit);

    /**
     * 获取缓存对象（自动反序列化）
     * @param key 缓存键
     * @param clazz 对象类型
     * @return 对象实例，不存在返回null
     */
    <T> T getObject(String key, Class<T> clazz);
}
