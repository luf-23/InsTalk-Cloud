package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.po.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

public interface UserAPI {

    @GetMapping("/info/email")
    User getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/info/username")
    User getUserByUsername(@RequestParam("username") String username);

    @GetMapping("/info/id")
    User getUserById(@RequestParam("id") Long id);
    
    @PostMapping("/info/ids")
    List<User> getUsersByIds(@RequestBody List<Long> ids);

    @PostMapping("/add/user")
    User add(@RequestBody User user);

    @PostMapping("/addRobot")
    User addRobot(@RequestBody User robot);

    @GetMapping("/info/nameLike")
    List<User> getUserByNameLike(@RequestParam String usernameLike);
}
