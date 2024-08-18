package com.ai.controller;

import com.ai.entity.User;
import com.ai.exceptions.CustomException;
import com.ai.service.IUserService;
import com.ai.util.Result;
import com.ai.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/internal-service/user-info")
public class UserInfoInternalServiceController {

    @Resource
    private IUserService userService;

    /**
     * 分页批量显示用户列表与信息
     */
    @GetMapping("list")
    public Result<List<UserVo>> userList(@RequestParam Integer current, Integer size){
        Page<User> page = new Page<>(current, size);
        page.addOrder(OrderItem.desc("id"));
        return userService.userList(page, new QueryWrapper<User>());
    }

    /**
     * 管理员获取通过user_id获取用户的信息
     */
    @GetMapping("{id}")
    public Result<User> userData(@PathVariable String id) throws CustomException {
        User user = userService.getById(id);
        if (user == null){
            throw new CustomException("The user with id:"+ id +"does not exist.");
        }
        return Result.success(user);
    }
}
