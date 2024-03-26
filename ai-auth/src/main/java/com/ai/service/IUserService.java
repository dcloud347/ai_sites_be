package com.ai.service;

import com.ai.dto.LoginDto;
import com.ai.entity.User;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-12
 */
public interface IUserService extends IService<User> {

    Result<LoginVo> login(LoginDto loginDto);

    Result<LoginVo> register(LoginDto loginDto);

    Result<LoginVo> registerByUsername(LoginDto loginDto);
}
