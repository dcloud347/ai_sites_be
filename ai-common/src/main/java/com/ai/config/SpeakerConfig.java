package com.ai.config;

import lombok.Data;

/**
 * @author 刘晨
 */
@Data
public class SpeakerConfig {
    /**
     * 音箱设备会话保存时间，单位：分钟
     */
    public static Integer sessionActive = 15;

}
