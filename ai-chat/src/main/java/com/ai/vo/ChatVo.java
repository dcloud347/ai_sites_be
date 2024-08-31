package com.ai.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 刘晨
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ChatVo {
    /**
     * 回复的消息
     */
    private String message;

    private Long sessionId;

    private String model;

    private Integer surplus;

    private String title;

    private JSONArray toolCalls;
}
