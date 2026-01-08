package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.UserAPI;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * User服务的Feign客户端
 * 通过Nacos服务发现调用user-service的内部API
 * 统一放在common模块供所有服务使用
 */
@FeignClient(name = "instalk-user-service", path = "/internal/user")
public interface UserFeignClient extends UserAPI {
    // 继承UserAPI的所有方法，无需重新定义
}
