package com.ai.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.util.DigestUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 后台管理员表
 * </p>
 *
 * @author 刘晨
 * @since 2024-05-06
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 管理员的实际姓名
     */
    private String name;

    /**
     * 密码
     */
    @Pattern(regexp = "^[a-zA-Z0-9]{6,18}$", message = "密码必须是字母和数字，不能用特殊符号，6-18位")
    private String password;

    /**
     * 管理员的电子邮箱，可用于找回密码等通讯
     */
    @Email
    private String email;

    @NotNull
    @Pattern(regexp = "admin|superAdmin|guest", message = "不认识的角色")
    private String role;

    public String getPassword() {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}
