package org.instalk.cloud.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class RequestContextUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_TOKEN_JTI = "X-Token-Jti";
    private static final String HEADER_CLAIM_PREFIX = "X-Claim-";

    /**
     * 获取当前请求
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HEADER_USERNAME);
    }

    /**
     * 获取Token JTI
     */
    public static String getTokenJti() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HEADER_TOKEN_JTI);
    }

    /**
     * 获取自定义的Claim值
     */
    public static String getClaimValue(String claimKey) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(HEADER_CLAIM_PREFIX + claimKey);
    }

    /**
     * 检查请求头中是否包含指定的claim
     */
    public static boolean hasClaim(String claimKey) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }
        String headerValue = request.getHeader(HEADER_CLAIM_PREFIX + claimKey);
        return headerValue != null && !headerValue.isEmpty();
    }
}
