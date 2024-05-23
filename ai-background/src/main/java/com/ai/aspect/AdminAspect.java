package com.ai.aspect;


import com.ai.annotation.RoleRequired;
import com.ai.entity.AdminEntity;
import com.ai.entity.Manager;
import com.ai.enums.RedisPrefixEnum;
import com.ai.service.IManagerService;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author 刘晨
 */
@Aspect
@Component
public class AdminAspect {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        String accessToken = request.getHeader("token");
        if(accessToken == null) {
            CommonUtil.sendJsonMessage(response, Result.error("未登录"));
            return null;
        }
        String adminId = stringRedisTemplate.opsForValue().get(RedisPrefixEnum.ADMIN_TOKEN.getPrefix() + accessToken);
        if(adminId != null){
            AdminEntity adminEntity = new AdminEntity();
            adminEntity.setId(Integer.valueOf((adminId)));
            // 校验权限
            Manager manager = managerService.getById(adminId);
            if (!Arrays.toString(roleEnums).contains(manager.getRole())){
                CommonUtil.sendJsonMessage(response, Result.error("权限不足"));
                return null;
            }
            //通过threadLocal传递用户登录信息
            threadLocal.set(adminEntity);
            // 如果已经登录，则继续执行方法
            return joinPoint.proceed();
        }
        CommonUtil.sendJsonMessage(response, Result.error("未登录"));
        return null;
    }

    @After("adminRequired()")
    public void afterLoginRequiredMethods() {
        // 确保在方法执行完毕后清理ThreadLocal
        threadLocal.remove();
    }
}

