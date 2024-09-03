package com.ai.aspect;

import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import com.ai.enums.Type;
import com.ai.exceptions.ServerException;
import com.ai.model.LoginEntity;
import com.ai.exceptions.CustomException;
import com.ai.model.Payload;
import com.ai.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class LoginAspect {
    public static ThreadLocal<LoginEntity> threadLocal = new ThreadLocal<>();

    @Resource
    private JwtUtil jwtUtil;

    @Pointcut("@annotation(com.ai.annotation.LoginRequired)")
    public void loginRequired() {
        // 定义切点
    }

    @Around("loginRequired()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String accessToken = request.getHeader("Authorization");
        Payload payload;
        try{
            payload = jwtUtil.getPayloadFromJwt(accessToken);
        }catch (ServerException e){
            throw new CustomException(e.getMessage());
        }
        if(!payload.getLoginType().equals(LoginType.USER) && !payload.getLoginType().equals(LoginType.ROBOT)){
            throw new CustomException("Permission Denied");
        }
        if(payload.getJwtType()!= JwtType.access_token){
            throw new CustomException("Please use access token for accessing resources.");
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

