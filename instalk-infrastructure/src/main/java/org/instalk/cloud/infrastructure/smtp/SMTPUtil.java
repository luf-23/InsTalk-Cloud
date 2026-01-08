package org.instalk.cloud.infrastructure.smtp;
import org.instalk.cloud.infrastructure.redis.RedisUtilImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.instalk.cloud.common.util.StringUtil.generateCaptcha;

@Component
public class SMTPUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    public boolean sendMessage(String toEmail,String subject,String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
