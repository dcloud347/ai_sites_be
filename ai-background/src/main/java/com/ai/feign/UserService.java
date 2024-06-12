package com.ai.feign;


import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 管理员获取通过user_id获取用户的信息
     */
    @GetMapping("/api/userinfo/{id}")
    ResponseEntity<Result> userData(@PathVariable String id);
}
