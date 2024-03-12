package com.ai.feign;


import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 刘晨
 */

@Component
@FeignClient(value = "ai-notify")
public interface EmailService {
    /**
     * 发送验证码
     * @param email 邮箱
     * @param code  验证码
     */
    @PostMapping("notify/sendCode")
    Result sendCode(@RequestBody String email, @RequestParam("code") String code);
}
