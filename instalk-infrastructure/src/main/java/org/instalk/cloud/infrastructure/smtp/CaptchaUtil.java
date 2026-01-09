package org.instalk.cloud.infrastructure.smtp;

import org.instalk.cloud.common.util.RedisUtil;
import org.instalk.cloud.common.util.SMTPUtil;
import org.instalk.cloud.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CaptchaUtil {

    // 验证码过期时间（分钟）
    private static final long EXPIRE_MINUTES = 5;
    // Redis key 前缀
    private static final String CAPTCHA_KEY_PREFIX = "email:captcha:";

    @Autowired
    private SMTPUtil smtpUtil;
    @Autowired
    private RedisUtil redisUtil;

    public boolean sendCaptcha(String toEmail) {
        try {
            String captcha = StringUtil.generateCaptcha();
            String key = CAPTCHA_KEY_PREFIX + toEmail;
            redisUtil.set(key, captcha, EXPIRE_MINUTES, TimeUnit.MINUTES);
            SimpleMailMessage message = new SimpleMailMessage();
            String subject = "验证码";
            String content = "您的验证码是：" + captcha + "，" + EXPIRE_MINUTES + "分钟内有效。请勿泄露给他人。";
            smtpUtil.sendMessage(toEmail, subject, content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyCaptcha(String email, String captcha) {
        String key = CAPTCHA_KEY_PREFIX + email;
        String savedCaptcha = redisUtil.get(key);
        if (savedCaptcha != null && savedCaptcha.equals(captcha)) return true;
        return false;
    }

}
