package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.MessageAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "messageFeignClient", name = "instalk-chat-service", path = "/internal/message")
public interface MessageFeignClient extends MessageAPI {

}
