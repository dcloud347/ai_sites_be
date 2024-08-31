package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class FunctionApiVo {
    private String name;
    private String description;
    private ParametersApiVo parameters;
}
