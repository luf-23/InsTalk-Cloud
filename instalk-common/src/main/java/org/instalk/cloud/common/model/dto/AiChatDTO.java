package org.instalk.cloud.common.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatDTO {
    private String taskId;
    private Long robotId;
    private Long currentUserMessageId;
    private List<Long> messageIds;

    //该类只在后端构建用，不用前端传递
    @Data
    public static class AiChatMessage{
        private String role;
        private String content;
    }
}
