package com.ai.vo;

import com.ai.entity.File;
import com.ai.util.LocalDateTimeFormatterUtils;
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
    private String created_at;
    private String url;

    public FileVo(File file){
        id = file.getId();
        filename = file.getFilename();
        bytes = file.getBytes();
        created_at = LocalDateTimeFormatterUtils.localDateTimeToString(file.getCreatedAt());
        url = url;
    }
}
