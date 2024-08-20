package com.ai.vo;

import com.ai.enums.JwtType;
import com.ai.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenVo {
    private boolean isValid;
    private JwtType jwtType;
    private LoginType loginType;
}
