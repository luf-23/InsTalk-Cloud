package org.instalk.cloud.instalkaiconfigservice.controller;

import org.instalk.cloud.common.feign.api.AiConfigAPI;
import org.instalk.cloud.common.model.dto.internal.AiConfigDTO;
import org.instalk.cloud.instalkaiconfigservice.service.UserAiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/aiconfig")
public class InternalController implements AiConfigAPI {

    @Autowired
    private UserAiConfigService userAiConfigService;

    @Override
    public void add(@RequestBody AiConfigDTO aiConfigDTO) {
        userAiConfigService.add(aiConfigDTO.getUserId(), aiConfigDTO.getRobotId());
    }
}
