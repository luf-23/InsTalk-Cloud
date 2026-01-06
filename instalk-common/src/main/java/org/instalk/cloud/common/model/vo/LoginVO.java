package org.instalk.cloud.common.model.vo;

import lombok.Data;
import org.instalk.cloud.common.model.po.User;

import java.time.LocalDateTime;

//auth/login
@Data
public class LoginVO {
    private Long id;
    private String username;
    private String signature;
    private String email;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
    private String accessToken;
    private String refreshToken;

    public LoginVO(User user, String accessToken, String refreshToken){
        this.id = user.getId();
        this.username = user.getUsername();
        this.signature = user.getSignature();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
