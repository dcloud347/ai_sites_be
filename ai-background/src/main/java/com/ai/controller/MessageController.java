package com.ai.controller;

import com.ai.annotation.RoleRequired;
import com.ai.feign.MessageService;
import com.ai.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping("session/{id}")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result sessions(@PathVariable String id){
        return messageService.select(id);
    }

    @GetMapping("chat/{id}")
    @RoleRequired({RoleRequired.RoleEnum.admin, RoleRequired.RoleEnum.superAdmin})
    public Result getChat(@PathVariable String id){
        return messageService.getChat(id);
    }
}
