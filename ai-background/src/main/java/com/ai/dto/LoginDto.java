package com.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.DigestUtils;

/**
 * @author 刘晨
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    private String username;
    private String password;

    public String getPassword() {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}
