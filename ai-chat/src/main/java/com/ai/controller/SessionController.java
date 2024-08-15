package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.exceptions.CustomException;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
    public ResponseEntity<Result> create(@RequestBody(required = false) Session session, HttpServletRequest request){
        if (session == null){
            session = new Session();
        }
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        // 判断该用户之前是不是有new chat记录
        List<Session> list = sessionService.list(new QueryWrapper<Session>().eq("user_id", loginEntity.getUserId()).eq("title", "new chat"));
        if (list == null || !list.isEmpty()){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("Repetitive conversation"));
        }
        session.setTitle("new chat");
        session.setUserId(loginEntity.getUserId());
        session.setStartTime(LocalDateTime.now());
        session.setType(loginEntity.getType());
        sessionService.save(session);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        return ResponseEntity.ok().body(Result.success(map));
    }

    /**
     * 删除指定的session
     */
    @DeleteMapping
    @LoginRequired
    public ResponseEntity<Result> delete(@RequestParam String id) throws CustomException {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        Session session = sessionService.getById(id);
        if (session == null){
            throw new CustomException("The session corresponding to this ID does not exist");
        }else if (session.getUserId() != loginEntity.getUserId()){
            throw new CustomException("You have no right to delete someone else's conversation");
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
        sessionService.remove(new QueryWrapper<Session>().eq("title", "new chat"));
    }

    /**
     * 查询某用户的所有会话记录
     */
    @GetMapping("user/{id}")
    public Result<List<Session>> select(@PathVariable String id){
        List<Session> list = sessionService.list(new QueryWrapper<Session>().eq("user_id", id).orderByDesc("id"));
        return Result.success(list);
    }
}
