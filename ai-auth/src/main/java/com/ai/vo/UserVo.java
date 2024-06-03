package com.ai.vo;

import com.ai.entity.User;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author 刘晨
 */
@Data
public class UserVo {
    private String nick;
    private String email;
    private String lastIp;
    private String password;
    private LocalDate lastDate;
    private String code;
    public UserVo(User user){
        nick = user.getNick();
        email = user.getEmail();
        lastIp = user.getLastIp();
        password = user.getPassword();
        lastDate = user.getLastDate();
        code = user.getCode();
    }
}
