package com.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘晨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadVo {

    private String object;
    private String id;
    private String purpose;
    private String filename;
    private Long bytes;
    private long created_at;
    private String status;
    private String status_details;
}
