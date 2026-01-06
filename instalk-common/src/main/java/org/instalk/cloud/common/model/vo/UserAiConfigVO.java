package org.instalk.cloud.common.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserAiConfigVO {
    private String systemPrompt;
    private String model;
    private Float temperature;
    private Integer maxTokens;//不可修改
    private Float topP;
    private Float presencePenalty;
    private Integer seed;
    private Integer dailyMessageLimit;
    private Integer dailyMessageCount;
    private LocalDate lastResetDate;
    private Integer totalMessages;
    private Long totalTokensUsed;
    private LocalDateTime lastUsedAt;
}
