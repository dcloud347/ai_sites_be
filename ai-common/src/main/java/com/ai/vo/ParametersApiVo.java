package com.ai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ParametersApiVo {
    private String type="object";
    private Map<String,PropertyDescriptionApiVo> properties = new HashMap<>();
    private List<String> required = new ArrayList<>();
    private boolean additionalProperties;

    public void addProperty(String name, String type, String description){
        PropertyDescriptionApiVo property = new PropertyDescriptionApiVo();
        property.setType(type);
        property.setDescription(description);
        properties.put(name, property);
    }

    public void addRequired(String name){
        required.add(name);
    }
}
