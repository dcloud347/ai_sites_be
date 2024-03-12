package com.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
public class LoginDto {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;

}
