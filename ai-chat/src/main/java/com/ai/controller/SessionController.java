package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/session")
public class SessionController {
    @Resource
    private ISessionService sessionService;
    /**
     * 创建一个新的session
     */
    @PostMapping
    @LoginRequired
    public Result create(@RequestBody(required = false) Session session, HttpServletRequest request){
        if (session == null){
            session = new Session();
        }
        session.setTitle("new chat");
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        session.setUserId(loginEntity.getUserId());
        sessionService.save(session);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        return Result.success(map);
    }

    /**
     * 删除指定的session
     */
    @DeleteMapping
    @LoginRequired
    public ResponseEntity<Result> delete(@RequestParam String id){
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        Session session = sessionService.getById(id);
        if (session == null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("The session corresponding to this ID does not exist"));
        }else if (session.getUserId() != loginEntity.getUserId()){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("You have no right to delete someone else's conversation"));
        }
        sessionService.removeById(id);
        return ResponseEntity.ok().body(Result.success());
    }

    /**
     * 定时删除空session，5分钟一次
     */
    @Scheduled(fixedRate = 1000 * 60 * 5)
    @Lazy(value = false)
    public void delete(){
        System.out.println("---------------");
        System.out.println("执行定时任务");
        sessionService.remove(new QueryWrapper<Session>().eq("title", "new chat"));
    }
}
