package com.ai.dto;

import com.ai.enums.Type;
import lombok.Data;

import java.util.List;

/**
 * @author 刘晨
 */

@Data
public class ChatDto {

    /**
     * 会话id
     */
    private Long sessionId;
    private Type type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String mode;

    private List<String> fileId;
}
