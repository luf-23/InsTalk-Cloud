package org.instalk.cloud.common.util;

public interface SMTPUtil {
    boolean sendMessage(String toEmail,String subject,String content);
}
