package org.instalk.cloud.instalkmessageservice.controller;

import org.instalk.cloud.common.feign.api.MessageAPI;
import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.common.model.dto.internal.MessageStatusDTO;
import org.instalk.cloud.common.model.po.Message;
import org.instalk.cloud.instalkmessageservice.mapper.MessageMapper;
import org.instalk.cloud.instalkmessageservice.mapper.MessageStatusMapper;
import org.instalk.cloud.instalkmessageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/message")
public class InternalController implements MessageAPI {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageStatusMapper messageStatusMapper;

    @Override
    public void deleteById1AndId2(@RequestBody DeleteMessageDTO deleteMessageDTO) {
        messageService.deleteById1AndId2(deleteMessageDTO);
    }

    @Override
    public Message getById(@RequestParam Long messageId) {
        return messageMapper.selectById(messageId);
    }

    @Override
    public List<Message> getByIds(@RequestBody List<Long> messageIds) {
        return messageMapper.selectByIds(messageIds);
    }

    @Override
    public Message addPrivateMessage(@RequestBody Message assistantMessage) {
        messageMapper.addPrivateMessage(assistantMessage);
        return assistantMessage;
    }

    @Override
    public void addStatus(@RequestBody MessageStatusDTO messageStatusDTO) {
        messageStatusMapper.add(messageStatusDTO.getMessageId(), messageStatusDTO.getUserId());
    }

    @Override
    public void addStatusAndRead(@RequestBody MessageStatusDTO messageStatusDTO) {
        messageStatusMapper.addAndRead(messageStatusDTO.getMessageId(), messageStatusDTO.getUserId());
    }

    @Override
    public Boolean getStatus(@RequestBody MessageStatusDTO messageStatusDTO) {
        return messageStatusMapper.select(messageStatusDTO.getMessageId(), messageStatusDTO.getUserId());
    }
}
