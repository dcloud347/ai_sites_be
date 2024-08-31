package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ToolApiVo {
    private String type="function";
    private FunctionApiVo function;



}
