package com.ai.controller;

import com.ai.annotation.RoleRequired;
import com.ai.feign.MessageService;
import com.ai.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping("session/{id}")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result<List<Object>> sessions(@PathVariable String id){
        return messageService.select(id);
    }

    @GetMapping("chat/{id}")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result<List<Object>> getChat(@PathVariable String id){
        return messageService.getChat(id);
    }
}
