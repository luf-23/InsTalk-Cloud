package org.instalk.cloud.common.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface TokenUtil {

    public void add(String jti);

    public Boolean exist(String jti);
}
