package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 潘越
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class SpeechAudioVo {
    /**
     * 音频
     */
    private byte[] audio;

}
