package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface MessageAPI {

    @PostMapping("/deleteByUserId")
    void deleteById1AndId2(@RequestBody DeleteMessageDTO deleteMessageDTO);
}
