package com.ai.controller;

import com.ai.service.IMessageService;
import com.ai.util.Result;
import com.ai.vo.ChatRecordVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author 潘越
 * @since 2024-03-12

 */
@RestController
@RequestMapping("/internal-service/message")
public class MessageInternalServiceController {

    @Resource
    private IMessageService messageService;

    /**
     * 查询某用户会话下的聊天记录
     */
    @GetMapping("chat/{id}")
    public Result<List<ChatRecordVo>> getChat(@PathVariable String id){
        List<ChatRecordVo> record = messageService.record(id);
        return Result.success(record);
    }
}
