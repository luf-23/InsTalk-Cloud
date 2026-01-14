package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.AiAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "instalk-aiconfig-service", path = "/internal/ai")
public interface AiFeignClient extends AiAPI {
}
