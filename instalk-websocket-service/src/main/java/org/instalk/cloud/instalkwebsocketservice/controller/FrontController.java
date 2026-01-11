package org.instalk.cloud.instalkwebsocketservice.controller;

import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.instalkwebsocketservice.service.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
 @RequestMapping("/ws")
public class FrontController {
    @Autowired
    private WebSocketHandler webSocketHandler;

    /**
     * 检查用户是否在线
     */
    @GetMapping("/online/{userId}")
    public Result<Boolean> isUserOnline(@PathVariable Long userId) {
        boolean online = webSocketHandler.isUserOnline(userId);
        return Result.success(online);
    }

    /**
     * 批量检查用户在线状态
     */
    @PostMapping("/online/batch")
    public Result<Map<Long, Boolean>> batchCheckOnline(@RequestBody List<Long> userIds) {
        Map<Long, Boolean> onlineStatus = webSocketHandler.getOnlineUsers();
        return Result.success(onlineStatus);
    }

    /**
     * 获取所有在线用户
     */
    @GetMapping("/online/all")
    public Result<Map<Long, Boolean>> getAllOnlineUsers() {
        Map<Long, Boolean> onlineUsers = webSocketHandler.getOnlineUsers();
        return Result.success(onlineUsers);
    }
}
