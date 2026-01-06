package org.instalk.cloud.common.model.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String captcha;
    private String password;
}
