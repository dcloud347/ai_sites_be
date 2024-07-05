package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        String ip = CommonUtil.getIpAddr(request);
        Mono<LocalDateTime> dateTime = this.sessionService.getTimeZone(ip);
        LocalDateTime localDateTime = dateTime.block();
        session.setTitle(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).setStartTime(localDateTime);
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        session.setUserId(loginEntity.getUserId());
        sessionService.save(session);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getId());
        return Result.success(map);
    }


}
