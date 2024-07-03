package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.ai.vo.SessionVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Resource
    private ISessionService sessionService;
    @GetMapping()
    @LoginRequired
    public Result<List<SessionVo>> sessionSearch(@RequestParam String q){
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        QueryWrapper<Session> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginEntity.getUserId());
        wrapper.like("title", q);
        List<Session> sessionList = sessionService.list(wrapper);
        ArrayList<SessionVo> sessionVos = new ArrayList<>();
        sessionList.forEach(session -> sessionVos.add(new SessionVo(session)));
        return Result.success(sessionVos);
    }

}
