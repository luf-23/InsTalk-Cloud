package org.instalk.cloud.infrastructure.smtp;

import org.instalk.cloud.common.util.SMTPUtil;
import org.instalk.cloud.infrastructure.config.MailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


@Component
@ComponentScan(basePackageClasses = {MailConfig.class})
public class SMTPUtilImpl implements SMTPUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:3162794813@qq.com}")
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
