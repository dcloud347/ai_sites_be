package com.ai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    private Long id;

    /**
     * 用户的id
     */
    private Integer userId;

    /**
     * 会话的标题
     */
    private String title;

    /**
     * 会话开始时间
     */
    private LocalDateTime startTime;
    private String type;
}
