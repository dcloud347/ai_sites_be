package com.ai.aspect;

import com.ai.annotation.LoginRequired;
import com.ai.interceptor.LoginInterceptor;
import com.ai.model.LoginEntity;
import com.ai.util.CommonUtil;
import com.ai.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class LoginAspect {

    @Pointcut("@annotation(com.ai.annotation.LoginRequired)")
    public void loginRequired() {
        // 定义切点
    }

    @Around("loginRequired()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 从LoginInterceptor的ThreadLocal中获取登录信息
        LoginEntity loginEntity = LoginInterceptor.threadLocal.get();

        if (loginEntity == null) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            // 获取响应对象，用于返回错误信息
            if (attrs != null && attrs.getResponse() != null) {
                CommonUtil.sendJsonMessage(attrs.getResponse(), Result.error("未登录"));
            }
            return null; // 直接返回，不再继续执行方法
        }
        // 如果已经登录，则继续执行方法
        return joinPoint.proceed();
    }
}

