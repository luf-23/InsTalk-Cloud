package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.FriendshipAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "friendshipFeignClient", name = "instalk-social-service", path = "/internal/friendship")
public interface FriendshipFeignClient extends FriendshipAPI {

}
