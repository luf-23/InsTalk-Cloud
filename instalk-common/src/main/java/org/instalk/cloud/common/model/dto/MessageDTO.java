package org.instalk.cloud.common.model.dto;

import lombok.Data;

@Data
public class MessageDTO {
    //receiverId和groupId只有一个不为空
    private Long receiverId;
    private Long groupId;
    private String content;
    private String messageType;
}
