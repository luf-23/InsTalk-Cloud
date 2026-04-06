package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "webSocketFeignClient", name = "instalk-chat-service", path = "/internal/ws")
public interface WebSocketFeignClient extends WebSocketAPI {

}
