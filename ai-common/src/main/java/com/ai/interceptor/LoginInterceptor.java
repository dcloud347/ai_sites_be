package com.ai.interceptor;


import com.ai.model.LoginEntity;
import com.ai.enums.RedisPrefixEnum;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 刘晨
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<LoginEntity> threadLocal = new ThreadLocal<>();

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String accessToken = request.getHeader("token");
        if(accessToken == null) {
            CommonUtil.sendJsonMessage(response, Result.error("未登录"));
            return false;
        }
        String user_id = stringRedisTemplate.opsForValue().get(RedisPrefixEnum.USER_TOKEN.getPrefix() + accessToken);
        if(user_id != null){
            LoginEntity loginEntity = new LoginEntity();
            loginEntity.setUserId(Long.parseLong(user_id));
            //通过attribute传递用户信息
            //request.setAttribute("loginUser",loginUser);

            //通过threadLocal传递用户登录信息
            threadLocal.set(loginEntity);
            return true;
        }
        CommonUtil.sendJsonMessage(response, Result.error("未登录"));
        return false;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
}
