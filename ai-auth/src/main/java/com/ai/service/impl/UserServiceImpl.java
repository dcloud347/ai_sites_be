package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.dto.LoginDto;
import com.ai.dto.RefreshTokenDto;
import com.ai.dto.UserInfoDto;
import com.ai.dto.VerifyTokenDto;
import com.ai.entity.User;
import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import com.ai.exceptions.CustomException;
import com.ai.exceptions.ServerException;
import com.ai.mapper.UserMapper;
import com.ai.model.LoginEntity;
import com.ai.model.Payload;
import com.ai.service.IUserService;
import com.ai.util.CommonUtil;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.LoginVo;
import com.ai.vo.UserInfoVo;
import com.ai.vo.UserVo;
import com.ai.vo.VerifyTokenVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ai.util.GoogleUtil;
import com.ai.util.UserCredentialsGenerator;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 潘越
 * @since 2024-03-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     *普通用户接口
     */
    @Override
    public ResponseEntity<Result<LoginVo>> login(LoginDto loginDto, HttpServletRequest request) {
        return login_(loginDto,request,LoginType.USER);
    }

    /**
     * 注册接口
     */
    @Override
    public ResponseEntity<Result<LoginVo>> register(LoginDto loginDto) {
        String code = stringRedisTemplate.opsForValue().get(loginDto.getEmail());
        // 检查验证码
        if (!loginDto.getCode().equals(code)){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("verification code incorrect"));
        }
        // 查询该邮箱是否已经注册
        if (this.getOne(new QueryWrapper<User>().eq("email", loginDto.getEmail())) != null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("The email has been registered."));
        }
        User user = new User(loginDto);
        this.save(user);
        String access_token = JwtUtil.generateJwtToken(user.getId(),LoginType.USER, JwtType.access_token);
        String refreshToken = JwtUtil.generateJwtToken(user.getId(),LoginType.USER,JwtType.refresh_token);
        return ResponseEntity.ok(Result.success(new LoginVo(access_token, refreshToken)));
    }

    @Override
    public Result<Object> updateUserInfo(UserInfoDto userInfoDto) {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        User user = this.getById(loginEntity.getUserId());
        user.setNick(userInfoDto.getNick());
        user.setAvatar_url(userInfoDto.getAvatar());
        this.updateById(user);
        return Result.success();
    }

    @Override
    public Result<UserInfoVo> userInfo() {
        LoginEntity loginEntity = LoginAspect.threadLocal.get();
        User user = this.getById(loginEntity.getUserId());
        return Result.success(new UserInfoVo(user));
    }

    @Override
    public Result<List<UserVo>> userList(Page<User> page, QueryWrapper<User> userVoQueryWrapper) {
        Page<User> userPage = this.page(page, userVoQueryWrapper);
        List<User> records = userPage.getRecords();
        List<UserVo> list = new ArrayList<>();
        records.forEach(user -> list.add(new UserVo(user)));
        return Result.success(list);
    }

    @Override
    public ResponseEntity<Result<LoginVo>> googleLogin(String token, HttpServletRequest request){
        //未完成
        String email = GoogleUtil.get_email(token);
        if(email==null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("Authentication Failed."));
        }
        User one = this.getOne(new QueryWrapper<User>().eq("email", email));
        if(one==null){
            String username = "user_"+UserCredentialsGenerator.generateUsername(8);
            String password = UserCredentialsGenerator.generatePassword(12);
            one = new User(username,password,email);
            this.save(one);
        }
        String ip = CommonUtil.getIpAddr(request);
        one.setLastIp(ip);
        this.updateById(one);
        String access_token = JwtUtil.generateJwtToken(one.getId(),LoginType.USER, JwtType.access_token);
        String refreshToken = JwtUtil.generateJwtToken(one.getId(),LoginType.USER,JwtType.refresh_token);
        return ResponseEntity.ok(Result.success(new LoginVo(access_token, refreshToken)));
    }

    /**
     * 刷新令牌接口
     */
    @Override
    public ResponseEntity<Result<LoginVo>> refreshToken(RefreshTokenDto refreshTokenDto, HttpServletRequest request) throws CustomException {
        Payload payload;
        try{
            payload = JwtUtil.getPayloadFromJwt(refreshTokenDto.getRefreshToken());
        }catch (ServerException e){
            throw new CustomException("Refresh "+e.getMessage());
        }
        if(!payload.getJwtType().equals(JwtType.refresh_token)){
            throw new CustomException("Please use refresh token to refresh!");
        }
        if(!payload.getLoginType().equals(LoginType.USER) && !payload.getLoginType().equals(LoginType.ROBOT)){
            throw new CustomException("Permission Denied");
        }
        User user = this.getById(payload.getAccountId());
        String ip = CommonUtil.getIpAddr(request);
        user.setLastIp(ip);
        user.setLastDate(LocalDate.now());
        this.updateById(user);
        String access_token = JwtUtil.generateJwtToken(payload.getAccountId(),payload.getLoginType(), JwtType.access_token);
        String refreshToken = JwtUtil.generateJwtToken(payload.getAccountId(),payload.getLoginType(), JwtType.refresh_token);
        return ResponseEntity.ok(Result.success(new LoginVo(access_token, refreshToken)));
    }

    /**
     *音响登录接口
     */
    @Override
    public ResponseEntity<Result<LoginVo>> speakerLogin(LoginDto loginDto, HttpServletRequest request) {
        return login_(loginDto,request,LoginType.ROBOT);
    }

    /**
     * 验证令牌接口
     */
    @Override
    public Result<VerifyTokenVo> VerifyToken(VerifyTokenDto verifyTokenDto) {
        String token = verifyTokenDto.getToken();
        VerifyTokenVo verifyTokenVo = new VerifyTokenVo();
        Payload payload;
        try{
            payload = JwtUtil.getPayloadFromJwt(token);
        }catch (ServerException e){
            verifyTokenVo.setValid(false);
            return Result.success(verifyTokenVo);
        }
        verifyTokenVo.setValid(true);
        verifyTokenVo.setJwtType(payload.getJwtType());
        verifyTokenVo.setLoginType(payload.getLoginType());
        return Result.success(verifyTokenVo);
    }

    /**
     * general登录
     */
    private ResponseEntity<Result<LoginVo>> login_(LoginDto loginDto, HttpServletRequest request, LoginType loginType) {
        User user = new User(loginDto);
        User one;
        one = this.getOne(new QueryWrapper<User>().eq("email", loginDto.getEmailOrUsername()).eq("password", user.getPassword()));
        if(one == null){
            one = this.getOne(new QueryWrapper<User>().eq("username", loginDto.getEmailOrUsername()).eq("password", user.getPassword()));
        }
        if (one == null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("Username or password incorrect."));
        }
        // 修改上次登录的ip
        String ip = CommonUtil.getIpAddr(request);
        one.setLastIp(ip);
        one.setLastDate(LocalDate.now());
        this.updateById(one);
        String access_token = JwtUtil.generateJwtToken(one.getId(),loginType, JwtType.access_token);
        String refreshToken = JwtUtil.generateJwtToken(one.getId(),loginType,JwtType.refresh_token);
        return ResponseEntity.ok(Result.success(new LoginVo(access_token, refreshToken)));
    }
}
