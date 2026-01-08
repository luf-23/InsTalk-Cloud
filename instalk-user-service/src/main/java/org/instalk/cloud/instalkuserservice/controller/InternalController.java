package org.instalk.cloud.instalkuserservice.controller;

import org.instalk.cloud.common.feign.api.UserAPI;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.instalkuserservice.service.InternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/user")
public class InternalController implements UserAPI {

    @Autowired
    private InternalService internalService;

    @Override
    public User getUserByEmail(String email) {
        if (email== null) return null;
        return internalService.getUserByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        if (username== null) return null;
        return internalService.getUserByUsername(username);
    }
}
