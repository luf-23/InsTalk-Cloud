package org.instalk.cloud.common.model.dto.internal;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.vo.MessageVO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsBroadcastMessageDTO {
    private List<Long> receiverIds;
    private MessageVO messageVO;
}
