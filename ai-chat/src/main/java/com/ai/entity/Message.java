package com.ai.entity;

import com.ai.dto.ChatDto;
import com.ai.vo.ChatVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

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
 * @author 
 * @since 2024-03-14
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 角色，gpt或者用户
     */
    private String role;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 模型
     */
    private String model;

    /**
     * 文件id
     */
    private String fileId;

    private String fileUrl;
    public Message(ChatDto chatDto){
        content = chatDto.getContent();
        messageType = chatDto.getMessageType();
        sessionId = chatDto.getSessionId();
        createTime = LocalDateTime.now();
        if(chatDto.getFileId()!=null){
            fileId = String.join(",", chatDto.getFileId());
        }else{
            fileId = null;
        }
    }

    public Message(ChatVo chatVo){
        content = chatVo.getMessage();
        messageType = "text";
        sessionId = chatVo.getSessionId();
        createTime = LocalDateTime.now();
    }
    public Message(String content,long sessionId){
        this.content = content;
        this.messageType = "text";
        this.sessionId = sessionId;
        this.createTime = LocalDateTime.now();
    }

    public Message(File file){
        this.content = file.getFilename();
        this.messageType = "file";
        createTime = LocalDateTime.now();
        fileUrl = file.getUrl();
    }
}
