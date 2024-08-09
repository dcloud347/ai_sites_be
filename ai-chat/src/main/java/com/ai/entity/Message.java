package com.ai.entity;

import com.ai.dto.ChatDto;
import com.ai.enums.Role;
import com.ai.enums.Type;
import com.ai.vo.ChatVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.EnumValue;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 消息表
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
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话id
     */
    private Long sessionId;


    /**
     * 角色，system, user 或 assistant
     */
    @EnumValue
    private Role role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模型
     */
    private String model;

    /**
     * 类型
     */
    @EnumValue
    private Type type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public Message(ChatDto chatDto){
        content = chatDto.getContent();
        sessionId = chatDto.getSessionId();
        createTime = LocalDateTime.now();
    }

    public Message(ChatVo chatVo){
        content = chatVo.getMessage();
        sessionId = chatVo.getSessionId();
        createTime = LocalDateTime.now();
    }
    public Message(String content,long sessionId){
        this.content = content;
        this.sessionId = sessionId;
        this.createTime = LocalDateTime.now();
    }
}
