package org.instalk.cloud.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关白名单配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthWhiteListProperties {

    private List<String> whiteList;
}
