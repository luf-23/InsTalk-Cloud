package org.instalk.cloud.common.model.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsBroadcastRevokeDTO {
    private List<Long>  receiverIds;
     private Long messageId;
}
