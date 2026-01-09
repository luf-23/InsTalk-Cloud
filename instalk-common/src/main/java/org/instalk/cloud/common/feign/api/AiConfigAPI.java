package org.instalk.cloud.common.feign.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AiConfigAPI {

    @GetMapping("/add")
    void add(@RequestParam("userId") Long userId, @RequestParam("robotId") Long robotId);
}
