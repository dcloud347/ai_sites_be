package com.ai.aspect;


import com.ai.annotation.RoleRequired;
import com.ai.entity.AdminEntity;
import com.ai.entity.Manager;
import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import com.ai.exceptions.ServerException;
import com.ai.model.Payload;
import com.ai.service.IManagerService;
import com.ai.util.CommonUtil;

import com.ai.util.JwtUtil;
import com.ai.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class AdminAspect {

    @Resource
    private IManagerService managerService;

    public static ThreadLocal<AdminEntity> threadLocal = new ThreadLocal<>();

    @Pointcut("@annotation(com.ai.annotation.RoleRequired)")
    public void adminRequired() {
        // 定义切点
    }

    @Around("adminRequired()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //1. 通过连接点获取方法签名 被切入方法的所有信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //2.获取被切入方法对象
        Method method = signature.getMethod();
        //3.获取方法上的注解
        RoleRequired roleRequired = method.getAnnotation(RoleRequired.class);
        RoleRequired.RoleEnum[] roleEnums = roleRequired.value();
        //4.获取注解的值
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String accessToken = request.getHeader("Authorization");
        Payload payload;
        try{
            payload = JwtUtil.getPayloadFromJwt(accessToken);
        }catch (ServerException e){
            response.setStatus(Result.error().getCode());
            CommonUtil.sendJsonMessage(response, Result.error("Access "+e.getMessage()));
            return false;
        }
        if(!payload.getLoginType().equals(LoginType.ADMIN)){
            response.setStatus(Result.error().getCode());
            CommonUtil.sendJsonMessage(response, Result.error("Permission Denied"));
            return false;
        }
        if(payload.getJwtType()!= JwtType.access_token){
            response.setStatus(Result.error().getCode());
            CommonUtil.sendJsonMessage(response, Result.error("Please use access token for accessing resources."));
            return false;
        }
        AdminEntity adminEntity = new AdminEntity();
        adminEntity.setId(payload.getAccountId());
        // 校验权限
        Manager manager = managerService.getById(payload.getAccountId());
        if (!Arrays.toString(roleEnums).contains(manager.getRole())){
            CommonUtil.sendJsonMessage(response, Result.error("Permission Denied"));
            return null;
        }
        //通过threadLocal传递用户登录信息
        threadLocal.set(adminEntity);
        // 如果已经登录，则继续执行方法
        return joinPoint.proceed();
    }

    @After("adminRequired()")
    public void afterLoginRequiredMethods() {
        // 确保在方法执行完毕后清理ThreadLocal
        threadLocal.remove();
    }
}

