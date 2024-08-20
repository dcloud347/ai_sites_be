package com.ai.service;

import com.ai.dto.LoginDto;
import com.ai.dto.RefreshTokenDto;
import com.ai.dto.UserInfoDto;
import com.ai.dto.VerifyTokenDto;
import com.ai.entity.User;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.ai.vo.UserInfoVo;
import com.ai.vo.UserVo;
import com.ai.vo.VerifyTokenVo;
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
 * @author 潘越
 * @since 2024-03-12
 */
public interface IUserService extends IService<User> {

    ResponseEntity<Result<LoginVo>> login(LoginDto loginDto,  HttpServletRequest request);

    ResponseEntity<Result<LoginVo>> register(LoginDto loginDto);

    Result<Object> updateUserInfo(UserInfoDto userInfoDto);

    Result<UserInfoVo> userInfo();

    Result<List<UserVo>> userList(Page<User> page, QueryWrapper<User> userVoQueryWrapper);

    ResponseEntity<Result<LoginVo>> googleLogin(String token, HttpServletRequest request);

    ResponseEntity<Result<LoginVo>> refreshToken(RefreshTokenDto refreshTokenDto, HttpServletRequest request);

    ResponseEntity<Result<LoginVo>> speakerLogin(LoginDto loginDto,  HttpServletRequest request);

    Result<VerifyTokenVo> VerifyToken(VerifyTokenDto verifyTokenDto);

}
