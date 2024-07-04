package com.ai.service;

import com.ai.dto.LoginDto;
import com.ai.dto.UserInfoDto;
import com.ai.entity.User;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.ai.vo.UserInfoVo;
import com.ai.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-12
 */
public interface IUserService extends IService<User> {

    ResponseEntity<Result<LoginVo>> login(LoginDto loginDto,  HttpServletRequest request);

    ResponseEntity<Result<LoginVo>> register(LoginDto loginDto);

    ResponseEntity<Result<LoginVo>> registerByUsername(LoginDto loginDto);

    ResponseEntity<Result<LoginDto>> generate();

    Result updateUserInfo(UserInfoDto userInfoDto);

    Result<UserInfoVo> userInfo();

    Result<List<UserVo>> userList(Page<User> page, QueryWrapper<User> userVoQueryWrapper);

    ResponseEntity<Result<LoginVo>> googleLogin(String token, HttpServletRequest request);
}
