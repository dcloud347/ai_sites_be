package com.ai.controller;

import com.ai.dto.LoginDto;
import com.ai.feign.EmailService;
import com.ai.service.IUserService;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import com.ai.vo.LoginVo;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-03-12

 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private IUserService userService;
    @Resource
    private EmailService emailService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 邮箱-密码登录
     */
    @PostMapping("login")
    public ResponseEntity<Result<LoginVo>> login(@RequestBody LoginDto loginDto,  HttpServletRequest request){
        return userService.login(loginDto, request);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestBody LoginDto loginDto){
        String code = CommonUtil.getRandomCode(4);
        // 发送验证码
        emailService.sendCode(loginDto.getEmail(), code);
        // 存验证码到redis
        stringRedisTemplate.opsForValue().set(loginDto.getEmail(), code, 30, TimeUnit.MINUTES);
        return Result.success();
    }
    /**
     * 邮箱-密码-验证码注册
     */
    @PostMapping("register")
    public ResponseEntity<Result<LoginVo>> register(@RequestBody LoginDto loginDto){
        return userService.register(loginDto);
    }

    /**
     * 账号密码注册
     */
    @PostMapping("registerByUsername")
    public ResponseEntity<Result<LoginVo>> registerByUsername(@RequestBody LoginDto loginDto){
        return userService.registerByUsername(loginDto);
    }

    /**
     * 生成账号密码 （供音箱使用）
     */
    @PostMapping("generate")
    public ResponseEntity<Result<LoginDto>> generate(){
        return userService.generate();
    }
}
