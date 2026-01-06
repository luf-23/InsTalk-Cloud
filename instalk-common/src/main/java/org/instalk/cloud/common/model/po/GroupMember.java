package org.instalk.cloud.common.model.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMember {
    private Long id;
    private Long userId;
    private Long groupId;
    private String role;
    private LocalDateTime joinedAt;
}
