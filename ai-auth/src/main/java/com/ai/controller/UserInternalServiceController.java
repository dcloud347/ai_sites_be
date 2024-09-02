package com.ai.controller;

import com.ai.entity.User;
import com.ai.exceptions.CustomException;
import com.ai.service.IUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author 潘越
 * @since 2024-03-12

 */
@RestController
@RequestMapping("/internal-service/user")
public class UserInternalServiceController {

    @Resource
    private IUserService userService;

    /**
     * 查询余额
     */
    @GetMapping("tokens/{id}")
    public Integer tokens(@PathVariable Integer id){
        User user = userService.getById(id);
        if(user == null){
            throw new CustomException("User not Found!");
        }
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
