package org.instalk.cloud.common.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatDTO {
    private String taskId;
    private Long robotId;
    private Long currentUserMessageId;
    private List<Long> messageIds;
    private Integer windowSize;
    private Integer summaryTriggerSize;
    /** @deprecated 记忆检索已改为对话中的 search_memories 工具，此字段不再使用 */
    @Deprecated
    private Integer ragTopK;
    private Boolean includeSummary;
    /** @deprecated 同上，不再注入静态 RAG 上下文 */
    @Deprecated
    private Boolean includeRag;

    //该类只在后端构建用，不用前端传递
    @Data
    public static class AiChatMessage{
        private String role;
        private String content;
    }
}
