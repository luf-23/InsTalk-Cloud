package org.instalk.cloud.common.model.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.vo.MessageVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsSendPrivateMessageDTO {
    private Long receiverId;
    private MessageVO messageVO;
}
