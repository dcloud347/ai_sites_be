package com.ai.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ai-auth")
@Component
public interface UserService {
    @GetMapping("/api/user/tokens/{id}")
    Integer getTokens(@PathVariable Integer id);
    @GetMapping("/api/user/setTokens/{id}")
    void setTokens(@RequestParam Integer tokens, @PathVariable Integer id);
}
