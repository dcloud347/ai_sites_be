package com.ai.dto;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author 刘晨
 */

@Data
@Accessors(chain = true)
public class ToolDto {

    /**
     * 会话id
     */
    private String toolName;

    /**
     * 模型名称
     */
    private String url;
}

