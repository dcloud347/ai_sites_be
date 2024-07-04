package com.ai.service.impl;

import com.ai.dto.LoginDto;
import com.ai.entity.Manager;
import com.ai.enums.RedisPrefixEnum;
import com.ai.mapper.ManagerMapper;
import com.ai.service.IManagerService;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager> implements IManagerService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result<LoginVo> login(LoginDto loginDto) {
        Manager one = this.getOne(new QueryWrapper<Manager>().eq("email", loginDto.getEmail()).eq("password", loginDto.getPassword()));
        if (one == null){
            return Result.error("Username or password incorrect.");
        }
        LoginVo loginVo = new LoginVo().setToken(genToken(one)).setRole(one.getRole());
        return Result.success(loginVo);
    }

    @Override
    public Result create(Manager manager) {
        // 检查是否已有该用户名
        if (this.getOne(new QueryWrapper<Manager>().eq("email", manager.getEmail())) != null){
            return Result.error("The email has been registered.");
        }
        this.save(manager);
        return Result.success();
    }

    private String genToken(Manager manager){
        HashMap<String, Object> map = new HashMap<>(1);
        map.put("id", manager.getId());
        String token = JwtUtil.generateJwtToken(map);
        // 将token存入redis
        stringRedisTemplate.opsForValue().set(RedisPrefixEnum.ADMIN_TOKEN.getPrefix() + token, manager.getId().toString(), 10, TimeUnit.DAYS);
        return token;
    }
}
