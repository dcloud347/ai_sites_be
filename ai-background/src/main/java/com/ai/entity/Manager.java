package com.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.intellij.lang.annotations.Pattern;
import org.springframework.util.DigestUtils;

import javax.validation.constraints.Email;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-22
 */
@Getter
@Setter
@Accessors(chain = true)
public class Manager implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 邮箱
     */
    @Email
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色
     */
    private String role;

    /**
     * 管理员姓名，备注用
     */
    private String name;

    public String getPassword() {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}
