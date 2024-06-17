package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    public Result create(@RequestBody(required = false) Session session){
        if (session == null){
            session = new Session();
        }
        session.setTitle(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setStartTime(LocalDateTime.now());
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        session.setUserId(loginEntity.getUserId());
        sessionService.save(session);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        return Result.success(map);
    }

}
