package org.instalk.cloud.infrastructure.redis;

import org.instalk.cloud.common.util.JwtUtil;
import org.instalk.cloud.common.util.RedisUtil;
import org.instalk.cloud.common.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenUtilImpl implements TokenUtil {

    private static final String KEY_PREFIX = "expired_refresh_token:";

    private static final long EXPIRE_TIME = JwtUtil.REFRESH_EXPIRE_TIME/1000;

    @Autowired
    private RedisUtil redisUtil;

    public void add(String jti) {
        redisUtil.set(KEY_PREFIX+jti,"",EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public Boolean exist(String jti){
        return redisUtil.hasKey(KEY_PREFIX+jti);
    }
}
