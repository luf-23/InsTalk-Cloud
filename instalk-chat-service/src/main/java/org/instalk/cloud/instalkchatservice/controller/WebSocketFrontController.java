package org.instalk.cloud.instalkchatservice.controller;

import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.instalkchatservice.service.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws")
public class WebSocketFrontController {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @GetMapping("/online/{userId}")
    public Result<Boolean> isUserOnline(@PathVariable Long userId) {
        boolean online = webSocketHandler.isUserOnline(userId);
        return Result.success(online);
    }
}
