package org.instalk.cloud.common.model.po;

import lombok.Data;
import org.instalk.cloud.common.model.enums.AiMemoryType;

import java.time.LocalDateTime;

@Data
public class AiMemory {
    private Long id;
    private Long userId;
    private Long robotId;
    private AiMemoryType type;
    private String content;
    private LocalDateTime createdAt;
}
