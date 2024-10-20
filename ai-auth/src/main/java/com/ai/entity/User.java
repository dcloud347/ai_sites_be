package com.ai.entity;

import com.ai.dto.LoginDto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author 
 * @since 2024-03-12
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 注册时间
     */
    private LocalDate createDate;

    /**
     * 上次登录的日期
     */
    private LocalDate lastDate;

    /**
     * 积分
     */
    private Integer point;

    /**
     * 上次登录的ip地址
     */
    private String lastIp;
    public LocalDate getCreateDate() {
        if (createDate == null){
            createDate = LocalDate.now();
        }
        return createDate;
    }

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nick;
    /**
     * 头像
     */
    private String avatar_url;

    /**
     * 音箱编号
     */
    private String code;
    private Integer tokens;
    public User (LoginDto loginDto){
        email = loginDto.getEmail();
        username = loginDto.getUsername();
        password = DigestUtils.md5DigestAsHex(loginDto.getPassword().getBytes());
    }

    public User(String username, String password, String email){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public static void main(String[] args) {
        User user = new User("123", "test@outlook.com", DigestUtils.md5DigestAsHex(("test").getBytes()));
        System.out.println(user);
    }
}
