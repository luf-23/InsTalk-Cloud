package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.common.model.po.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface MessageAPI {

    @PostMapping("/deleteByUserId")
    void deleteById1AndId2(@RequestBody DeleteMessageDTO deleteMessageDTO);

    @GetMapping("/getById")
    Message getById(@RequestParam Long messageId);
}
