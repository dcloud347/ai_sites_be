package com.ai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.ai.enums.Type;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会话表
 * </p>
 *
 * @author 
 * @since 2024-03-14
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户的id
     */
    private Integer userId;

    /**
     * 会话的标题
     */
    private String title;
    private Boolean archive;

    /**
     * 会话开始时间
     */
    private LocalDateTime startTime;

    @EnumValue
    private Type type;


}
