package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.aspect.LoginAspect;
import com.ai.dto.LoginDto;
import com.ai.entity.User;
import com.ai.feign.EmailService;
import com.ai.model.LoginEntity;
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
     * google 登录/注册
     */
    @PostMapping(value = "/googleLogin")
    public ResponseEntity<Result<LoginVo>> googleLogin(@RequestBody String token, HttpServletRequest request){
        return userService.googleLogin(token, request);
    }

    /**
     * 注销账号
     */
    @PostMapping("cancel")
    @LoginRequired
    public Result cancel(){
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        userService.removeById(loginEntity.getUserId());
        return Result.success();
    }

    /**
     *  登出账号
     */

    @PostMapping("logout")
    @LoginRequired
    public Result<String> logout(@RequestHeader("Token") String token){
        return userService.logout(token);
    }

    /**
     * 邮箱-密码登录
     */
    @PostMapping("speaker-login")
    public ResponseEntity<Result<LoginVo>> speakerLogin(@RequestBody LoginDto loginDto,  HttpServletRequest request){
        return userService.login(loginDto, request);
    }

    /**
     * 查询余额
     */
    @GetMapping("tokens/{id}")
    public Integer tokens(@PathVariable Integer id){
        User user = userService.getById(id);
        return user.getTokens();
    }

    /**
     * 扣费
     */
    @GetMapping("setTokens/{id}")
    public void decrease(@RequestParam Integer tokens, @PathVariable Integer id){
        User user = userService.getById(id);
        user.setTokens(user.getTokens() - tokens);
        userService.updateById(user);
    }
}
