package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.entity.Session;
import com.ai.enums.RedisPrefixEnum;
import com.ai.mapper.SessionMapper;
import com.ai.model.LoginEntity;
import com.ai.service.ISessionService;
import com.ai.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 会话表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements ISessionService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result getSession() {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        String key = RedisPrefixEnum.SPEAKER_SESSION.getPrefix() + loginEntity.getUserId();
        Object o = redisTemplate.opsForValue().get(key);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sessionId", o);
        return Result.success(map);
    }
}
