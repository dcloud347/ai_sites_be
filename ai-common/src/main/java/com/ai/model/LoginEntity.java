package com.ai.model;

import lombok.Data;
import com.ai.enums.Type;
/**
 * @author 刘晨
 */
@Data
public class LoginEntity {

    private int userId;

    private Type type;

}
