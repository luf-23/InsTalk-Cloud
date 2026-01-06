package org.instalk.cloud.common.model.vo;

import lombok.Data;
import org.instalk.cloud.common.model.po.User;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String signature;
    private String avatar;

    public UserInfoVO(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.signature = user.getSignature();
        this.avatar = user.getAvatar();
    }
}
