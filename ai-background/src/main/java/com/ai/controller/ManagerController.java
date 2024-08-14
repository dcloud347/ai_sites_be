package com.ai.controller;

import com.ai.dto.LoginDto;
import com.ai.dto.RefreshTokenDto;
import com.ai.entity.Manager;
import com.ai.service.IManagerService;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 管理员表 前端控制器
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    @Resource
    private IManagerService managerService;

    /**
     * 管理员登录
     */
    @PostMapping("login")
    public Result<LoginVo> login(@RequestBody LoginDto loginDto) {
        return managerService.login(loginDto);
    }

    /**
     * 添加管理员账号
     */
    @PostMapping
    public Result create(@RequestBody Manager manager){
        return managerService.create(manager);
    }

    @PostMapping("refresh-token")
    public Result<LoginVo> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto){
        return managerService.refreshToken(refreshTokenDto);
    }
}
