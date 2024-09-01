package com.ai.entity;

import com.ai.dto.MessageDto;
import com.ai.enums.Role;
import com.ai.enums.Type;
import com.ai.vo.ChatResponse;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
 * @author 潘越
 * @since 2024-03-14
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "message", autoResultMap = true)
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话id
     */
    private Long sessionId;


    /**
     * 角色，system, user, assistant 或 tool
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
     * 对应调用工具消息ID Tool角色消息专用
     */
    private String ToolCallId;

    /**
     * 调用工具信息
     */

    @TableField(value="tool_call", typeHandler = JacksonTypeHandler.class)
    private JSONArray toolCall;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public Message(MessageDto messageDto){
        String content = messageDto.getContent();
        if(content!=null){
            content = content.strip();
        }
        this.content = content;
        this.role = messageDto.getRole();
        this.createTime = LocalDateTime.now();
    }


    public Message(ChatResponse chatResponse){
        String content = chatResponse.getContent();
        if(content!=null){
            content = content.strip();
        }
        this.content = content;
        this.role = Role.valueOf(chatResponse.getRole());
        this.createTime = LocalDateTime.now();
    }
}
