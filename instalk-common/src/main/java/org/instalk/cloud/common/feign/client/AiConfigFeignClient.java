package org.instalk.cloud.common.feign.client;

import org.instalk.cloud.common.feign.api.AiConfigAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "instalk-aiconfig-service", path = "/internal/aiconfig")
public interface AiConfigFeignClient extends AiConfigAPI {
}
