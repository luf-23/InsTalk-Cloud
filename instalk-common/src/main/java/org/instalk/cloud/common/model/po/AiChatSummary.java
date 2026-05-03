package org.instalk.cloud.common.model.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatSummary {
    private Long id;
    private Long userId;
    private Long robotId;
    private String summary;
    private Long lastMessageId;
    private LocalDateTime updatedAt;
}
