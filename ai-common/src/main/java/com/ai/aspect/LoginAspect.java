package com.ai.aspect;

import com.ai.annotation.LoginRequired;
import com.ai.enums.RedisPrefixEnum;
import com.ai.model.LoginEntity;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class LoginAspect {
    public static ThreadLocal<LoginEntity> threadLocal = new ThreadLocal<>();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Pointcut("@annotation(com.ai.annotation.LoginRequired)")
    public void loginRequired() {
        // 定义切点
    }

    @Around("loginRequired()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String accessToken = request.getHeader("token");
        if(accessToken == null) {
            CommonUtil.sendJsonMessage(response, Result.error("未登录"));
            return false;
        }
        String user_id = stringRedisTemplate.opsForValue().get(RedisPrefixEnum.USER_TOKEN.getPrefix() + accessToken);
        if(user_id != null){
            LoginEntity loginEntity = new LoginEntity();
            loginEntity.setUserId(Integer.parseInt(user_id));
            //通过attribute传递用户信息
            //request.setAttribute("loginUser",loginUser);
            //通过threadLocal传递用户登录信息
            threadLocal.set(loginEntity);
            // 如果已经登录，则继续执行方法
            return joinPoint.proceed();
        }
        CommonUtil.sendJsonMessage(response, Result.error("未登录"));
        return false;
    }

    @After("loginRequired()")
    public void afterLoginRequiredMethods() {
        // 确保在方法执行完毕后清理ThreadLocal
        threadLocal.remove();
    }
}

