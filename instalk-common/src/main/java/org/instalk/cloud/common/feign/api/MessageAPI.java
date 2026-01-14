package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.common.model.dto.internal.MessageStatusDTO;
import org.instalk.cloud.common.model.po.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

public interface MessageAPI {

    @PostMapping("/deleteByUserId")
    void deleteById1AndId2(@RequestBody DeleteMessageDTO deleteMessageDTO);

    @GetMapping("/getById")
    Message getById(@RequestParam Long messageId);

    @PostMapping("/getByIds")
    List<Message> getByIds(@RequestBody List<Long> messageIds);

    @PostMapping("/addPrivateMessage")
    Message addPrivateMessage(@RequestBody Message assistantMessage);

    @PostMapping("/addStatus")
    void addStatus(@RequestBody MessageStatusDTO messageStatusDTO);

    @PostMapping("/addStatusAndRead")
    void addStatusAndRead(@RequestBody MessageStatusDTO messageStatusDTO);

    @PostMapping("/getStatus")
    Boolean getStatus(@RequestBody MessageStatusDTO messageStatusDTO);
}
