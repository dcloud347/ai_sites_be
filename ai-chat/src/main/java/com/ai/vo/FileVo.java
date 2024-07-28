package com.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 潘越
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileVo {
    private String id;
    private String filename;
    private Long bytes;
    private long created_at;
    private String url;
}
