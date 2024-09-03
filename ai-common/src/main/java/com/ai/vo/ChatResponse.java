package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;


@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ChatResponse {

    private boolean success=true;

    private Integer total_tokens;

    private String content="";

    private List<ToolCallResponse> toolCalls;

    private String finishReason="";

    private String role="";

    public void addContent(String content) {
        this.content+=content;
    }

    public void addRole(String role) {
        this.role+=role;
    }
    public void addFinishedReason(String finishReason) {
        this.finishReason+=finishReason;
    }

    public void addToolCall(ToolCallResponse toolCall){
        if(toolCalls==null){
            toolCalls = new ArrayList<>();
        }
        toolCalls.add(toolCall);
    }

    public ToolCallResponse getToolCall(Integer index) {
        ToolCallResponse toolCall;
        try{
             toolCall = toolCalls.get(index);
        }catch (IndexOutOfBoundsException e){
            for(int i=0; i< index-toolCalls.size()+1; i++){
                addToolCall(new ToolCallResponse());
            }
            return getToolCall(index);
        }
        return toolCall;
    }



}
