package com.ai.feign;


import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author 刘晨
 */

@Component
@FeignClient(value = "ai-auth")
public interface UserService {
    /**
     * 分页批量显示用户列表与信息
     */
    @GetMapping("/api/userinfo/list")
    Result userList(@RequestParam Integer current,@RequestParam Integer size);
}
