package org.instalk.cloud.common.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface RedisUtil {
    void set(String key, String value, long timeout, TimeUnit unit);

    String get(String key);

    Boolean hasKey(String key);

    Boolean delete(String key);

    Long delete(Collection<String> keys);
}
