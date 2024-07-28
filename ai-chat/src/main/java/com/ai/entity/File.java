package com.ai.entity;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.ai.vo.UploadVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2024-05-24
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 关联消息id
     */
    private long messageId;

    /**
     * 目的
     */
    private String purpose;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件大小
     */
    private Long bytes;

    /**
     * 上传时间
     */
    private LocalDateTime createdAt;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态详情
     */
    private String statusDetails;

    private String url;

    public File(UploadVo uploadVo){
        id = uploadVo.getId();
        purpose = uploadVo.getPurpose();
        filename = uploadVo.getFilename();
        bytes = uploadVo.getBytes();
        // 将时间戳转换为Instant
        Instant instant = Instant.ofEpochSecond(uploadVo.getCreated_at());
        // 将Instant转换为LocalDateTime
        createdAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        status = uploadVo.getStatus();
        statusDetails = uploadVo.getStatus_details();
        url = uploadVo.getUrl();
    }
}
