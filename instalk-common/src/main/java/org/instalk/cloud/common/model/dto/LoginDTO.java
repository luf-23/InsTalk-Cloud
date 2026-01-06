package org.instalk.cloud.common.model.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String email;
    private String password;
}
