package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.dto.UserInfoDto;
import com.ai.feign.EmailService;
import com.ai.service.IUserService;
import com.ai.util.Result;
import com.ai.vo.UserInfoVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 刘晨
 */
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {
    @Resource
    private IUserService userService;

    /**
     * 用户修改资料
     */
    @PostMapping()
    @LoginRequired
    public Result updateUserInfo(@RequestBody UserInfoDto userInfoDto){
        return userService.updateUserInfo(userInfoDto);
    }

    /**
     * 用户获取自己的个人资料
     */
    @GetMapping()
    @LoginRequired
    public Result<UserInfoVo> userInfo(){
        return userService.userInfo();
    }

}
