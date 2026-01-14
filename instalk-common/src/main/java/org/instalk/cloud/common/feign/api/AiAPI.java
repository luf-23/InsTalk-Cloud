package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.AiConfigDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AiAPI {

    @PostMapping("/add")
    void add(@RequestBody AiConfigDTO aiConfigDTO);
}
