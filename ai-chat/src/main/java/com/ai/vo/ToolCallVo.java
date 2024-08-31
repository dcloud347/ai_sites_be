package com.ai.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ToolCallVo {
    private String id;
    private String type;
    private Map<String, Object> function = new HashMap<>();
}
