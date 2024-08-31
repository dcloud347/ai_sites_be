package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ChatApiVo {
    private String model;
    private List<MessageApiVo> messages = new ArrayList<>();
    private boolean stream = false;
    private List<ToolApiVo> tools = new ArrayList<>();

    public void addMessage(MessageApiVo messageApiVo){
        messages.add(messageApiVo);
    }
    public void addTextMessage(String text, String role){
        TextContentApiVo textContentApiVo = new TextContentApiVo().setText(text);
        List<ContentApiVo> contents = new ArrayList<>();
        contents.add(textContentApiVo);
        MessageApiVo messageApiVo = new MessageApiVo().setContent(contents).setRole(role);
        addMessage(messageApiVo);
    }
    public void addTool(String name,String description,ParametersApiVo parameters){
        FunctionApiVo functionApiVo = new FunctionApiVo();
        functionApiVo.setName(name);
        functionApiVo.setDescription(description);
        functionApiVo.setParameters(parameters);
        tools.add(new ToolApiVo().setFunction(functionApiVo));
    }

    public void addImageMessage(String image_url,String text){
        TextContentApiVo textContentApiVo = new TextContentApiVo().setText(text);
        ImageContentApiVo imageContentApiVo = new ImageContentApiVo().setImage_url(new ImageURL(image_url));
        List<ContentApiVo> contents = new ArrayList<>();
        contents.add(textContentApiVo);
        contents.add(imageContentApiVo);
        MessageApiVo messageApiVo = new MessageApiVo().setContent(contents).setRole("user");
        addMessage(messageApiVo);
    }
}
