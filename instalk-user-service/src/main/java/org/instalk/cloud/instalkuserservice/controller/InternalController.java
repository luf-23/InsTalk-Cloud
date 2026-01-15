package org.instalk.cloud.instalkuserservice.controller;

import org.instalk.cloud.common.feign.api.UserAPI;
import org.instalk.cloud.common.model.dto.internal.ListDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.instalkuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/user")
public class InternalController implements UserAPI {

    @Autowired
    private UserService userService;

    @Override
    public User getUserByEmail(@RequestParam("email") String email) {
        if (email== null) return null;
        return userService.getUserByEmail(email);
    }

    @Override
    public User getUserByUsername(@RequestParam("username") String username) {
        if (username== null) return null;
        return userService.getUserByUsername(username);
    }

    @Override
    public User getUserById(@RequestParam Long id) {
        return userService.getUserById(id);
    }

    @Override
    public List<User> getUsersByIds(@RequestBody ListDTO<Long> listDTO) {
        return userService.getUsersByIds(listDTO.getItems());
    }

    @Override
    public User add(@RequestBody User user) {
        return userService.add(user);
    }

    @Override
    public User addRobot(@RequestBody User robot) {
        return userService.addRobot(robot);
    }

    @Override
    public List<User> getUserByNameLike(@RequestParam String username) {
        return userService.getUserByNameLike(username);
    }
}
