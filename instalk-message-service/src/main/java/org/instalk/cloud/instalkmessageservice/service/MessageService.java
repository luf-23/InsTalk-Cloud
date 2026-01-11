package org.instalk.cloud.instalkmessageservice.service;

import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.instalkmessageservice.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private MessageMapper messageMapper;

    public void deleteById1AndId2(DeleteMessageDTO deleteMessageDTO) {
        messageMapper.deleteById1AndId2(deleteMessageDTO.getId1(), deleteMessageDTO.getId2());
    }
}
