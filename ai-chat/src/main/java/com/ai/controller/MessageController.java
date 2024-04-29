package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.dto.ChatDto;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.IMessageService;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.ChatRecordVo;
import com.ai.vo.ChatVo;
import com.ai.vo.SessionVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private ISessionService sessionService;

    /**
     * 发起聊天
     */
    @PostMapping
    @LoginRequired
    public ResponseEntity<Result<ChatVo>> chat(@RequestBody ChatDto chatDto){
        return messageService.chat(chatDto);
    }

    /**
     * 查询我的所有会话记录
     */
    @GetMapping
    @LoginRequired
    public Result<List<SessionVo>> list(){
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        ArrayList<SessionVo> sessionVos = new ArrayList<>();
        sessionService.list(new QueryWrapper<Session>().eq("user_id", loginEntity.getUserId()).orderByDesc("id")).forEach(session -> sessionVos.add(new SessionVo(session)));
        return Result.success(sessionVos);
    }

    @GetMapping("{id}")
    @LoginRequired
    public ResponseEntity<Result<List<ChatRecordVo>>> record(@PathVariable String id){
        // 只能查询自己的聊天记录，防越权攻击
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        Session session = sessionService.getById(id);
        if (session == null || session.getUserId() != loginEntity.getUserId()){
            return ResponseEntity.status(ResultCode.ERROR.getCode()).body(Result.error("无权查询"));
        }
        return messageService.record(id);
    }
}
