package org.instalk.cloud.common.model.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.vo.MessageVO;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMQ implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageType; // PRIVATE | GROUP
    private MessageVO messageVO;
    private List<Long> receiverIds;
    private Integer retryCount = 0;
}
