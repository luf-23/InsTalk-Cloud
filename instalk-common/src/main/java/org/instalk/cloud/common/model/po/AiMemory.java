package org.instalk.cloud.common.model.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMemory {
    private Long id;
    private Long userId;
    private Long robotId;
    private String type;
    private String content;
    private LocalDateTime createdAt;
}
