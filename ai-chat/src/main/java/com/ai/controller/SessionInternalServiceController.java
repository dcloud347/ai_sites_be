package com.ai.controller;

import com.ai.entity.Session;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/internal-service/session")
public class SessionInternalServiceController {

    @Resource
    private ISessionService sessionService;

    /**
     * 查询某用户的所有会话记录
     */
    @GetMapping("user/{id}")
    public Result<List<Session>> select(@PathVariable String id){
        List<Session> list = sessionService.list(new QueryWrapper<Session>().eq("user_id", id).orderByDesc("id"));
        return Result.success(list);
    }
}
