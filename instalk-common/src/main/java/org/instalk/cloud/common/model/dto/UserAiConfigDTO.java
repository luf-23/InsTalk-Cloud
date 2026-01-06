package org.instalk.cloud.common.model.dto;

import lombok.Data;

@Data
public class UserAiConfigDTO {
    private Long robotId;
    private String systemPrompt;
    private String model;
    private Float temperature;
    private Float topP;
    private Float presencePenalty;
    private Integer seed;
}
