package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.FriendshipAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "instalk-friendship-service", path = "/internal/friendship")
public interface FriendshipFeignClient extends FriendshipAPI {

}
