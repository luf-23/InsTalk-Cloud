package org.instalk.cloud.common.util;


public interface TokenUtil {

    void add(String jti);

    Boolean exist(String jti);
}
