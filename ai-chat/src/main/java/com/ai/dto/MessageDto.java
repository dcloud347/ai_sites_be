package com.ai.dto;

import com.ai.enums.Role;
import lombok.Data;

import java.util.List;


/**
 * @author 刘晨
 */

@Data
public class MessageDto {
    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private Role role;

    /**
     * 对应调用工具消息ID Tool角色消息专用
     */
    private String toolCallId;

    private List<String> fileId;
}
