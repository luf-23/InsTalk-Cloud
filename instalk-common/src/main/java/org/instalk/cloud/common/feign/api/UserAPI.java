package org.instalk.cloud.common.feign.api;

import feign.Headers;
import org.instalk.cloud.common.model.po.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserAPI {

    @GetMapping("/info/email")
    User getUserByEmail(@RequestParam String email);

    @GetMapping("/info/username")
    User getUserByUsername(@RequestParam String username);
}
