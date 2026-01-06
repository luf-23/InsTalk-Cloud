package org.instalk.cloud.common.model.vo;

import lombok.Data;
//auth/refresh
@Data
public class RefreshVO {
    private String accessToken;
    private String refreshToken;

    public RefreshVO(String accessToken, String newRefreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = newRefreshToken;
    }
}
