package com.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author 刘晨
 */

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {
    private String token;
    private String role;
}
