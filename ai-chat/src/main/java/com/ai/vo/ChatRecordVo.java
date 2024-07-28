package com.ai.vo;

import com.ai.entity.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
    private String role;

    private String model;
    private String url;
    private List<FileVo> files;
    public ChatRecordVo(Message msg){
        message = msg.getContent();
        role = msg.getRole().toString();
        model = msg.getModel();
    }
}
