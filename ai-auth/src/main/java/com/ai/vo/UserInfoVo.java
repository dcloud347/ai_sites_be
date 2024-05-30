package com.ai.vo;

import com.ai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {
    private String avatar;
    private String nick;

    public UserInfoVo(User user) {
        avatar = user.getAvatar();
        nick = user.getNick();
    }
}
