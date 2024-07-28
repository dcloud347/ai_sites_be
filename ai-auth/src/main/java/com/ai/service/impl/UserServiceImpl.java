package com.ai.service.impl;

import com.ai.aspect.LoginAspect;
import com.ai.dto.LoginDto;
import com.ai.dto.UserInfoDto;
import com.ai.entity.User;
import com.ai.enums.RedisPrefixEnum;
import com.ai.mapper.UserMapper;
import com.ai.model.LoginEntity;
import com.ai.service.IUserService;
import com.ai.util.CommonUtil;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import com.ai.vo.LoginVo;
import com.ai.vo.UserInfoVo;
import com.ai.vo.UserVo;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.ai.util.GoogleUtil;
import com.ai.util.UserCredentialsGenerator;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;
    @Override
    public ResponseEntity<Result<LoginVo>> login(LoginDto loginDto, HttpServletRequest request) {
        return login_(loginDto,request,RedisPrefixEnum.USER_TOKEN.getPrefix());
    }

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
        return ResponseEntity.ok(Result.success(new LoginVo(genToken(user,RedisPrefixEnum.USER_TOKEN.getPrefix()))));
    }

    @Override
    public  ResponseEntity<Result<LoginVo>> registerByUsername(LoginDto loginDto) {
        // 查询该用户名是否已经注册
        if (this.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername())) != null){
            return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("The username already exist."));
        }
        User user = new User(loginDto);
        this.save(user);
        return ResponseEntity.ok(Result.success(new LoginVo(genToken(user, RedisPrefixEnum.USER_TOKEN.getPrefix()))));
    }

    @Override
    public Result updateUserInfo(UserInfoDto userInfoDto) {
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
        return ResponseEntity.ok(Result.success(new LoginVo(genToken(one, RedisPrefixEnum.USER_TOKEN.getPrefix()))));
    }

    @Override
    public Result<String> logout(String token){
        stringRedisTemplate.delete(RedisPrefixEnum.USER_TOKEN.getPrefix() + token);
        return Result.success("");
    }

    @Override
    public ResponseEntity<Result<LoginVo>> speakerLogin(LoginDto loginDto, HttpServletRequest request) {
        return login_(loginDto,request,RedisPrefixEnum.ROBOT_TOKEN.getPrefix());
    }

    /**
     * general登录
     */

    private ResponseEntity<Result<LoginVo>> login_(LoginDto loginDto, HttpServletRequest request, String prefix){
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
        this.updateById(one);
        return ResponseEntity.ok(Result.success(new LoginVo(genToken(one, prefix))));
    }

    /**
     * 发布token
     */
    private String genToken(User user, String prefix){
        HashMap<String, Object> map = new HashMap<>(1);
        map.put("id", user.getId());
        String token = JwtUtil.generateJwtToken(map);
        // 将token存入redis
        stringRedisTemplate.opsForValue().set(prefix + token, user.getId().toString(),20, TimeUnit.DAYS);
        // 更新上次登录时间
        user.setLastDate(LocalDate.now());
        this.updateById(user);
        return token;
    }
}
