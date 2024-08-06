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
    private String avatar_url;
    private String nick;
    private Integer tokens;
    public UserInfoVo(User user) {
        avatar_url = user.getAvatar_url();
        nick = user.getNick();
        tokens = user.getTokens();
    }
}
