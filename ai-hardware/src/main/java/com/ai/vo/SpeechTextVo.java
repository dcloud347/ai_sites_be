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
public class SpeechTextVo {
    /**
     * 文字
     */
    private String content;

}
