package org.instalk.cloud.common.model.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.dto.UserDTO;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String signature;
    private String email;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String username,String email,String password){
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(User user, UserDTO userDTO) {
        this.id = user.getId();
        this.username = userDTO.getUsername() !=null ? userDTO.getUsername() : user.getUsername();
        this.password = userDTO.getPassword() !=null ? userDTO.getPassword() : user.getPassword();
        this.email = userDTO.getEmail() !=null ? userDTO.getEmail() : user.getEmail();
        this.signature = userDTO.getSignature() !=null ? userDTO.getSignature() : user.getSignature();
        this.avatar = userDTO.getAvatar() !=null ? userDTO.getAvatar() : user.getAvatar();
    }
}
