package org.instalk.cloud.common.util;

import java.util.Map;

public class ThreadLocalUtil {
    //提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }

    //存储键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }


    //清除ThreadLocal 防止内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }

    public static Long getId(){
        Map<String,Object> claims = ThreadLocalUtil.get();
        return (Long) claims.get("id");
    }
    public static String getUsername(){
        Map<String,Object> claims = ThreadLocalUtil.get();
        return (String) claims.get("username");
    }
}
