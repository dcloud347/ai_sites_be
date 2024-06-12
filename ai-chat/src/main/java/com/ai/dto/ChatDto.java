package com.ai.dto;

import lombok.Data;

/**
 * @author 刘晨
 */

@Data
public class ChatDto {

    /**
     * 会话id
     */
    private Long sessionId;
    private String type;
    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String mode;

    private String fileId;
}
