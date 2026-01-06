package org.instalk.cloud.common.model.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.dto.UserAiConfigDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAiConfig {
    private Long id;
    private Long userId;
    private Long robotId;

    private String systemPrompt;
    private String model;
    private Float temperature;
    private Integer maxTokens;
    private Float topP;
    private Float presencePenalty;
    private Integer seed;

    private Integer dailyMessageLimit;
    private Integer dailyMessageCount;
    private LocalDate lastResetDate;
    private Integer totalMessages;
    private Long totalTokensUsed;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserAiConfig(UserAiConfig userAiConfig, UserAiConfigDTO userAiConfigDTO) {
        this.userId = userAiConfig.getUserId();
        this.robotId = userAiConfig.getRobotId();
        this.systemPrompt = userAiConfigDTO.getSystemPrompt() != null ? userAiConfigDTO.getSystemPrompt() : userAiConfig.getSystemPrompt();
        this.model = userAiConfigDTO.getModel() != null ? userAiConfigDTO.getModel() : userAiConfig.getModel();
        this.temperature = userAiConfigDTO.getTemperature() != null ? userAiConfigDTO.getTemperature() : userAiConfig.getTemperature();
        this.topP = userAiConfigDTO.getTopP() != null ? userAiConfigDTO.getTopP() : userAiConfig.getTopP();
        this.presencePenalty = userAiConfigDTO.getPresencePenalty() != null ? userAiConfigDTO.getPresencePenalty() : userAiConfig.getPresencePenalty();
        this.seed = userAiConfigDTO.getSeed() != null ? userAiConfigDTO.getSeed() : userAiConfig.getSeed();
    }
}
