package com.ai.service.impl;

import com.ai.dto.LoginDto;
import com.ai.entity.User;
import com.ai.enums.RedisPrefixEnum;
import com.ai.mapper.UserMapper;
import com.ai.service.IUserService;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result<LoginVo> login(LoginDto loginDto) {
        User user = new User(loginDto);
        User one = this.getOne(new QueryWrapper<User>().eq("email", loginDto.getEmailOrUsername()).eq("password", user.getPassword()));
        User one1 = this.getOne(new QueryWrapper<User>().eq("username", loginDto.getEmailOrUsername()).eq("password", user.getPassword()));
        if (one == null && one1 == null){
            return Result.error("账号或密码错误");
        }
        return Result.success(one1 == null ? new LoginVo(genToken(one)) : new LoginVo(genToken(one1)));
    }

    @Override
    public Result<LoginVo> register(LoginDto loginDto) {
        String code = stringRedisTemplate.opsForValue().get(loginDto.getEmail());
        // 检查验证码
        if (!loginDto.getCode().equals(code)){
            return Result.error("验证码错误");
        }
        // 查询该邮箱是否已经注册
        if (this.getOne(new QueryWrapper<User>().eq("email", loginDto.getEmail())) != null){
            return Result.error("邮箱已注册");
        }
        User user = new User(loginDto);
        this.save(user);
        return Result.success(new LoginVo(genToken(user)));
    }

    @Override
    public Result<LoginVo> registerByUsername(LoginDto loginDto) {
        // 查询该用户名是否已经注册
        if (this.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername())) != null){
            return Result.error("用户名已存在");
        }
        User user = new User(loginDto);
        this.save(user);
        return Result.success(new LoginVo(genToken(user)));
    }

    /**
     * 发布token
     */
    private String genToken(User user){
        HashMap<String, Object> map = new HashMap<>(1);
        map.put("id", user.getId());
        String token = JwtUtil.generateJwtToken(map);
        // 将token存入redis
        stringRedisTemplate.opsForValue().set(RedisPrefixEnum.USER_TOKEN.getPrefix() + token, user.getId().toString());
        // 更新上次登录时间
        user.setLastDate(LocalDate.now());
        this.updateById(user);
        return token;
    }
}
