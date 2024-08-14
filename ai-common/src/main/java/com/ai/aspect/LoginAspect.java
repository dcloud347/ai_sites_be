package com.ai.aspect;

import com.ai.enums.LoginType;
import com.ai.enums.Type;
import com.ai.exceptions.ServerException;
import com.ai.model.LoginEntity;
import com.ai.model.Payload;
import com.ai.util.CommonUtil;
import com.ai.util.JwtUtil;
import com.ai.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class LoginAspect {
    public static ThreadLocal<LoginEntity> threadLocal = new ThreadLocal<>();

    @Pointcut("@annotation(com.ai.annotation.LoginRequired)")
    public void loginRequired() {
        // 定义切点
    }

    @Around("loginRequired()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String accessToken = request.getHeader("Authorization");
        Payload payload;
        try{
            payload = JwtUtil.getPayloadFromJwt(accessToken);
        }catch (ServerException e){
            response.setStatus(Result.error().getCode());
            CommonUtil.sendJsonMessage(response, Result.error(e.getMessage()));
            return false;
        }
        if(!payload.getLoginType().equals(LoginType.USER) && !payload.getLoginType().equals(LoginType.ROBOT)){
            response.setStatus(Result.error().getCode());
            CommonUtil.sendJsonMessage(response, Result.error("Permission Denied"));
            return false;
        }
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setUserId(payload.getAccountId());
        loginEntity.setLoginType(payload.getLoginType());
        if(payload.getLoginType().equals(LoginType.USER)){
            loginEntity.setType(Type.WEBAPP);
        }else{
            loginEntity.setType(Type.ROBOT);
        }
        threadLocal.set(loginEntity);
        // 如果已经登录，则继续执行方法
        return joinPoint.proceed();
    }

    @After("loginRequired()")
    public void afterLoginRequiredMethods() {
        // 确保在方法执行完毕后清理ThreadLocal
        threadLocal.remove();
    }
}

