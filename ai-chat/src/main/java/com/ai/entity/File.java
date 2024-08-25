package com.ai.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
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
 * @author 刘晨
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

    private String url;
}
