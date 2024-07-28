package com.ai.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ImageContentApiVo extends ContentApiVo{
    private final String type = "image_url";
    private ImageURL image_url;
}
