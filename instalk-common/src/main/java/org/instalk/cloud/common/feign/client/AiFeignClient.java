package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.AiAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "instalk-ai-service", path = "/internal/ai")
public interface AiFeignClient extends AiAPI {
}
