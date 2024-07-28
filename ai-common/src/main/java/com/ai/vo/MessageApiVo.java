package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MessageApiVo {
    private String role;
    private List<ContentApiVo> content = new ArrayList<>();

    public void addContent(ContentApiVo content_) {
        content.add(content_);
    }
    public void addTextContent(String text){
        TextContentApiVo textContentApiVo = new TextContentApiVo().setText(text);
        addContent(textContentApiVo);
    }
    public void addImageContent(String image_url){
        ImageContentApiVo imageContentApiVo = new ImageContentApiVo().setImage_url(image_url);
        addContent(imageContentApiVo);
    }
}
