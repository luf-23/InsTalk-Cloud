package org.instalk.cloud.instalkuserservice.controller;

import org.instalk.cloud.common.feign.client.UserFeignClient;
import org.instalk.cloud.common.model.dto.UserDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserInfoVO;
import org.instalk.cloud.instalkuserservice.mapper.UserMapper;
import org.instalk.cloud.instalkuserservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class FrontController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/info")
    public Result<UserInfoVO> getInfo(@RequestParam Long id) {
        if (id==null) return Result.error("id不能为空");
        User user = userMapper.selectById(id);
        if (user==null) return Result.error("用户不存在");
        return userService.getInfo(user);
    }

    @PostMapping("/update")
    public Result update(@RequestBody UserDTO userDTO){
        return userService.update(userDTO);
    }
}
