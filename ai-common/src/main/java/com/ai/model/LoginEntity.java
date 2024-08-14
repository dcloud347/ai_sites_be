package com.ai.model;

import com.ai.enums.LoginType;
import com.ai.enums.Type;
import lombok.Data;
/**
 * @author 刘晨
 */
@Data
public class LoginEntity {

    private int userId;

    private LoginType loginType;

    private Type type;
}
