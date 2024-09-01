package com.ai.vo;

import com.ai.entity.Message;
import com.ai.enums.Role;
import com.ai.enums.Type;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ChatRecordVo {
    /**
     * 回复的消息
     */
    private String message;
    /**
     * 角色
     */
    private Role role;
    private Type type;

    private String model;

    private JSONArray toolCalls;

    private String toolCallId;

    private LocalDateTime createTime;

    private List<FileVo> files;
    public ChatRecordVo(Message msg){
        message = msg.getContent();
        role = msg.getRole();
        model = msg.getModel();
        type = msg.getType();
        toolCalls = msg.getToolCall();
        toolCallId = msg.getToolCallId();
        createTime = msg.getCreateTime();
    }
}
