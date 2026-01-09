package org.instalk.cloud.instalkauthservice.service;

import jakarta.servlet.http.HttpServletResponse;
import org.instalk.cloud.common.feign.client.FriendshipFeignClient;
import org.instalk.cloud.common.feign.client.AiConfigFeignClient;
import org.instalk.cloud.common.feign.client.UserFeignClient;
import org.instalk.cloud.common.model.dto.LoginDTO;
import org.instalk.cloud.common.model.dto.RegisterDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.LoginVO;
import org.instalk.cloud.common.model.vo.RefreshVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.JwtUtil;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.common.util.TokenUtil;
import org.instalk.cloud.infrastructure.redis.TokenUtilImpl;
import org.instalk.cloud.infrastructure.smtp.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@ComponentScan(basePackageClasses = {TokenUtilImpl.class, CaptchaUtil.class})
public class AuthService {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AiConfigFeignClient aiConfigFeignClient;

    @Autowired
    private FriendshipFeignClient friendshipFeignClient;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private CaptchaUtil captchaUtil;

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

    public Result logout() {
        Map<String,Object> claims = ThreadLocalUtil.get();
        String jti = (String) claims.get("jti");
        tokenUtil.add(jti);
        return Result.success();
    }

    public Result<RefreshVO> refresh(String refreshToken, HttpServletResponse response) {
        Map<String,Object> claims;
        try{
            claims = JwtUtil.parseToken(refreshToken);
        }catch(Exception e){
            response.setStatus(401);
            return Result.error("token刷新失败");
        }
        String jti = (String) claims.get("jti");
        tokenUtil.add(jti);
        String newJti = UUID.randomUUID().toString().replace("-", "");
        claims.put("jti",newJti);
        String accessToken = JwtUtil.genAccessToken(claims);
        String newRefreshToken = JwtUtil.genRefreshToken(claims);
        return Result.success(new RefreshVO(accessToken,newRefreshToken));
    }

    public Result register(RegisterDTO registerDTO) {
        if (registerDTO.getUsername() == null) return Result.error("用户名不能为空");
        if (registerDTO.getEmail() == null) return Result.error("邮箱不能为空");
        if (registerDTO.getPassword() == null) return Result.error("密码不能为空");
        if (userFeignClient.getUserByUsername(registerDTO.getUsername()) != null) return Result.error("用户名已存在");
        if (userFeignClient.getUserByEmail(registerDTO.getEmail()) != null) return Result.error("邮箱已存在");
        if (!captchaUtil.verifyCaptcha(registerDTO.getEmail(),registerDTO.getCaptcha())) return Result.error("验证码错误");
        User user = new User(registerDTO.getUsername(),registerDTO.getEmail(),registerDTO.getPassword());
        userFeignClient.add(user);
        // 添加机器人
        User robot = new User();
        robot.setSignature(String.format("我是%s的ai助手",user.getUsername()));
        robot.setUsername(String.format("%s的ai助手",user.getUsername()));
        robot.setEmail(String.format("%s@robot.com",user.getEmail()));
        robot.setAvatar("https://luf-23.oss-cn-wuhan-lr.aliyuncs.com/ins_talk/ai.png");
        robot.setRole("ROBOT");
        robot.setPassword(user.getPassword());
        userFeignClient.addRobot(robot);
        aiConfigFeignClient.add(user.getId(), robot.getId());
        Long minId = Long.min(user.getId(),robot.getId());
        Long maxId = Long.max(user.getId(),robot.getId());
        friendshipFeignClient.makeFriendsWithRobot(minId, maxId);

        return Result.success();
    }

    public Result sendCaptcha(String email) {
        if (email == null) return Result.error("邮箱不能为空");
        if (userFeignClient.getUserByEmail(email) != null) return Result.error("邮箱已存在");
        captchaUtil.sendCaptcha(email);
        return Result.success();
    }
}
