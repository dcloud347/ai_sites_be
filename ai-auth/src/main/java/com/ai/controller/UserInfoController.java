package com.ai.controller;

import com.ai.annotation.LoginRequired;
import com.ai.dto.UserInfoDto;
import com.ai.entity.User;
import com.ai.feign.EmailService;
import com.ai.service.IUserService;
import com.ai.util.Result;
import com.ai.vo.UserInfoVo;
import com.ai.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 分页批量显示用户列表与信息
     */
    @GetMapping("list")
    public Result<List<UserVo>> userList(@RequestParam Integer current, Integer size){
        Page<User> page = new Page<>(current, size);
        page.addOrder(OrderItem.desc("id"));
        return userService.userList(page, new QueryWrapper<User>());
    }
}
