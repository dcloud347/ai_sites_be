package com.ai.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ToolCallResponse {

    private String id="";

    private String type="";

    private FunctionResponse function = new FunctionResponse();

    public void addId(String id){
        this.id+=id;
    }

    public void addType(String type){
        this.type+=type;
    }



}
