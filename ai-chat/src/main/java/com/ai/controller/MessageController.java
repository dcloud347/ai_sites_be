package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.dto.ChatDto;
import com.ai.service.IMessageService;
import com.ai.util.Result;
import com.ai.vo.ChatVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 消息表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private IMessageService messageService;

    /**
     * 发起聊天
     */
    @PostMapping
    @LoginRequired
    public Result<ChatVo> chat(@RequestBody ChatDto chatDto){
        return messageService.chat(chatDto);
    }

}
