package com.ai.controller;

import com.ai.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 刘晨
 */

@RestController
@RequestMapping("notify")
public class EmailController {
    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String sender;

    @PostMapping("sendCode")
    public Result sendCode(@RequestBody String email, String code){
        sendMail(email, code);
        return Result.success();
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage mimeMessage = new SimpleMailMessage();
        mimeMessage.setFrom(sender);
        mimeMessage.setTo(email);

        mimeMessage.setSubject("注册验证码");
        mimeMessage.setText(code);
        mailSender.send(mimeMessage);
    }
}
