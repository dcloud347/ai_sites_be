package com.ai.dto;

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

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    private List<String> fileId;
}
