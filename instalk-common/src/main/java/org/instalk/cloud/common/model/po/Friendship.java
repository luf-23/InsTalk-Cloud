package org.instalk.cloud.common.model.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long id;
    private Long userId1;
    private Long userId2;
    private Long requesterId;
    private String status;
    private LocalDateTime createdAt;
}
