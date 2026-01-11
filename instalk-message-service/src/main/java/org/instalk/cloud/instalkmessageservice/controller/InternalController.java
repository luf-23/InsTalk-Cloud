package org.instalk.cloud.instalkmessageservice.controller;

import org.instalk.cloud.common.feign.api.MessageAPI;
import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.instalkmessageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/message")
public class InternalController implements MessageAPI {

    @Autowired
    private MessageService messageService;

    @Override
    public void deleteById1AndId2(@RequestBody DeleteMessageDTO deleteMessageDTO) {
        messageService.deleteById1AndId2(deleteMessageDTO);
    }
}
