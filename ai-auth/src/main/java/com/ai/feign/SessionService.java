package com.ai.feign;


import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * @author 刘晨
 */

@Component
@FeignClient(value = "ai-auth")
public interface SessionService {
    /**
     * 清除某用户的所有会话记录
     */
    @DeleteMapping("/internal-service/session/user/{id}")
    void clear(@PathVariable String id);
}
