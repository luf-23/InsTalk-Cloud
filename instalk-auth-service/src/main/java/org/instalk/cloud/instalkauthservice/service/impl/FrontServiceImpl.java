package org.instalk.cloud.instalkauthservice.service.impl;

import org.instalk.cloud.common.feign.client.UserFeignClient;
import org.instalk.cloud.common.model.dto.LoginDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.LoginVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.JwtUtil;
import org.instalk.cloud.instalkauthservice.service.FrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class FrontServiceImpl implements FrontService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public Result<LoginVO> login(LoginDTO loginDTO) {
        User user = null;
        // 根据邮箱或用户名查询用户
        if (loginDTO.getEmail() != null){
            user = userFeignClient.getUserByEmail(loginDTO.getEmail());
        }else if (loginDTO.getUsername() != null){
            user = userFeignClient.getUserByUsername(loginDTO.getUsername());
        }
        
        if (user == null) return Result.error("用户不存在");
        if (!user.getPassword().equals(loginDTO.getPassword())) return Result.error("密码错误");
        
        String jti = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> claims = Map.of("id",user.getId(),"username",user.getUsername(),"jti",jti,"role",user.getRole());
        String accessToken = JwtUtil.genAccessToken(claims);
        String refreshToken = JwtUtil.genRefreshToken(claims);
        return Result.success(new LoginVO(user,accessToken,refreshToken));
    }
}
