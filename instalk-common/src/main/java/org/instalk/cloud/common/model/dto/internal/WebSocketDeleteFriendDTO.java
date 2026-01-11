package org.instalk.cloud.common.model.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketDeleteFriendDTO {
    private Long id;
    private Long myId;
}
