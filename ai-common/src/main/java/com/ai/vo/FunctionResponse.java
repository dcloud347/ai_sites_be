package com.ai.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class FunctionResponse {

    private String name="";

    private String arguments="";

    public void addName(String name) {
        this.name+=name;
    }

    public void addArguments(String arguments) {
        this.arguments+=arguments;
    }

}
