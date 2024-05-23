package com.ai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 刘晨
 */
@Target(ElementType.METHOD) // 表明该注解可以用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留注解信息
public @interface RoleRequired {
    enum RoleEnum {
        admin,
        superAdmin,
        guest
    }
    // 传哪些角色，就代表哪些角色可以访问
    RoleEnum[] value() ;
}
