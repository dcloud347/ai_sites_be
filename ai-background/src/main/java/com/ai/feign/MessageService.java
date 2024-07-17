package com.ai.feign;

import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Component
@FeignClient(value = "ai-chat")
public interface MessageService {
    /**
     * 查询某用户的所有会话记录
     */
    @GetMapping("/api/session/user/{id}")
    Result select(@PathVariable String id);
    @GetMapping("/api/message/chat/{id}")
    Result getChat(@PathVariable String id);
}
