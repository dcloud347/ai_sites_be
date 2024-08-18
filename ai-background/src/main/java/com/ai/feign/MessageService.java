package com.ai.feign;

import com.ai.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@Component
@FeignClient(value = "ai-chat")
public interface MessageService {
    /**
     * 查询某用户的所有会话记录
     */
    @GetMapping("/internal-service/session/user/{id}")
    Result<List<Object>> select(@PathVariable String id);
    @GetMapping("/internal-service/message/chat/{id}")
    Result<List<Object>> getChat(@PathVariable String id);
}
