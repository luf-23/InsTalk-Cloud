package org.instalk.cloud.instalkaiservice.controller;

import org.instalk.cloud.instalkaiservice.service.UserAiConfigService;
import org.instalk.cloud.common.feign.api.AiAPI;
import org.instalk.cloud.common.model.dto.internal.AiConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ai")
public class InternalController implements AiAPI {

    @Autowired
    private UserAiConfigService userAiConfigService;

    @Override
    public void add(@RequestBody AiConfigDTO aiConfigDTO) {
        userAiConfigService.add(aiConfigDTO.getUserId(), aiConfigDTO.getRobotId());
    }
}
