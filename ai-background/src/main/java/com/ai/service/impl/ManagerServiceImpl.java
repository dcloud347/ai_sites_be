package com.ai.service.impl;

import com.ai.dto.LoginDto;
import com.ai.dto.RefreshTokenDto;
import com.ai.entity.Manager;
import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import com.ai.exceptions.CustomException;
import com.ai.exceptions.ServerException;
import com.ai.mapper.ManagerMapper;
import com.ai.model.Payload;
import com.ai.service.IManagerService;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import com.ai.vo.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager> implements IManagerService {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Result<LoginVo> login(LoginDto loginDto) {
        Manager one = this.getOne(new QueryWrapper<Manager>().eq("email", loginDto.getEmail()).eq("password", loginDto.getPassword()));
        if (one == null){
            return Result.error("Username or password incorrect.");
        }
        String accessToken = jwtUtil.generateJwtToken(one.getId(),LoginType.ADMIN, JwtType.access_token);
        String refreshToken = jwtUtil.generateJwtToken(one.getId(),LoginType.ADMIN, JwtType.refresh_token);
        LoginVo loginVo = new LoginVo(accessToken,refreshToken,one.getRole());
        return Result.success(loginVo);
    }

    @Override
    public Result create(Manager manager) {
        // 检查是否已有该用户名
        if (this.getOne(new QueryWrapper<Manager>().eq("email", manager.getEmail())) != null){
            return Result.error("The email has been registered.");
        }
        this.save(manager);
        return Result.success();
    }

    @Override
    public Result<LoginVo> refreshToken(RefreshTokenDto refreshTokenDto) {
        Payload payload;
        try{
            payload = jwtUtil.getPayloadFromJwt(refreshTokenDto.getRefreshToken());
        }catch (ServerException e){
            throw new CustomException("Refresh "+e.getMessage());
        }
        if(!payload.getJwtType().equals(JwtType.refresh_token)){
            throw new CustomException("Please use refresh token to refresh!");
        }
        if(!payload.getLoginType().equals(LoginType.ADMIN)){
            throw new CustomException("Permission Denied");
        }
        String access_token = jwtUtil.generateJwtToken(payload.getAccountId(),payload.getLoginType(), JwtType.access_token);
        String refreshToken = jwtUtil.generateJwtToken(payload.getAccountId(),payload.getLoginType(), JwtType.refresh_token);
        Manager manager = this.getById(payload.getAccountId());
        return Result.success(new LoginVo(access_token, refreshToken,manager.getRole()));
    }
}
