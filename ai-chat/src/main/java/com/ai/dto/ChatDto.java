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

    /**
     * 模型名称
     */
    private String model;
}
