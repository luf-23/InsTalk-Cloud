package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.WebSocketAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "instalk-websocket-service", path = "/internal/ws")
public interface WebSocketFeignClient extends WebSocketAPI {

}
