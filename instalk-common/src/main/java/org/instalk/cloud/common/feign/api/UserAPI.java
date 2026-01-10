package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.po.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserAPI {

    @GetMapping("/info/email")
    User getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/info/username")
    User getUserByUsername(@RequestParam("username") String username);

    @PostMapping("/add/user")
    User add(@RequestBody User user);

    @PostMapping("/addRobot")
    User addRobot(@RequestBody User robot);
}
