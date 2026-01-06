package org.instalk.cloud.common.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.po.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FriendVO {
    private Long id;
    private String username;
    private String signature;
    private String role;//ROBOT or USER
    private String avatar;
    private LocalDateTime createdAt;

    public FriendVO(User user, LocalDateTime createdAt){
        this.id = user.getId();
        this.username = user.getUsername();
        this.signature = user.getSignature();
        this.role = user.getRole().equals("ROBOT") ? "ROBOT" : "USER";
        this.avatar = user.getAvatar();
        this.createdAt = createdAt;
    }

}
