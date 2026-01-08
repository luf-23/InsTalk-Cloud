package org.instalk.cloud.instalkauthservice.controller;

import org.instalk.cloud.common.model.dto.LoginDTO;
import org.instalk.cloud.common.model.vo.LoginVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.instalkauthservice.service.FrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class FrontController {

    @Autowired
    private FrontService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO){
        if (loginDTO == null) return Result.error("参数错误");
        if (loginDTO.getEmail()== null&& loginDTO.getUsername()== null) return Result.error("参数错误");
        if (loginDTO.getPassword()== null) return Result.error("参数错误");
        return authService.login(loginDTO);
    }
}
