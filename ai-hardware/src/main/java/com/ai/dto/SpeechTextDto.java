package com.ai.dto;

import lombok.Data;

/**
 * @author 刘晨
 */

@Data
public class SpeechTextDto {

    /**
     * 文字
     */
    private String content;

    /**
     * 模型名称
     */
    private String mode;

    /**
     * 声音选择
     */
    private String voice;
}