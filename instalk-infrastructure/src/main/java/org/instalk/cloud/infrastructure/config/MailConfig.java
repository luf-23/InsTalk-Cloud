package org.instalk.cloud.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.qq.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private Integer port;

    @Value("${spring.mail.username:3162794813@qq.com}")
    private String username;

    @Value("${spring.mail.password:hcsibxvpnsgzdhfg}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private Boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private Boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private Boolean starttlsRequired;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 邮件服务器配置
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        
        // 邮件属性配置
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth.toString());
        props.put("mail.smtp.starttls.enable", starttlsEnable.toString());
        props.put("mail.smtp.starttls.required", starttlsRequired.toString());
        
        return mailSender;
    }
}
