package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.GroupAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "groupFeignClient", name = "instalk-social-service", path = "/internal/group")
public interface GroupFeignClient extends GroupAPI {
}
