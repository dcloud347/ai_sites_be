package com.ai.vo;

import com.ai.entity.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
public class ChatRecordVo {
    /**
     * 回复的消息
     */
    private String message;
    /**
     * 角色
     */
    private String role;

    private String model;
    private String url;
    private String messageType;
    public ChatRecordVo(Message msg){
        message = msg.getContent();
        role = msg.getRole();
        model = msg.getModel();
        url = msg.getFileUrl();
        messageType = msg.getMessageType();
    }
}
