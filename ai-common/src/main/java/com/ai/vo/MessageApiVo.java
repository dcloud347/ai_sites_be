package com.ai.vo;

import com.alibaba.fastjson.JSONArray;
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
    private String tool_call_id;
    private JSONArray tool_calls;

    public void addContent(ContentApiVo content_) {
        content.add(content_);
    }
    public void addTextContent(String text){
        TextContentApiVo textContentApiVo = new TextContentApiVo().setText(text);
        addContent(textContentApiVo);
    }
    public void addImageContent(String image_url){
        ImageContentApiVo imageContentApiVo = new ImageContentApiVo().setImage_url(new ImageURL(image_url));
        addContent(imageContentApiVo);
    }
}
