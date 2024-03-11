package com.ai.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ObjectToMapUtils {
    /**
     * 将一个类查询方式加入map
     */
    public static Map<String, Object> setConditionMap(Object obj){
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();//获取类的各个属性值
        for(Field field : fields){
            String fieldName =  field.getName();//获取类的属性名称
            map.put(fieldName,  getValueByFieldName(fieldName,obj));

        }
        return map;
    }
    /**
     * 根据属性名获取该类此属性的值
     * @param fieldName
     * @param object
     * @return
     */
    private static Object getValueByFieldName(String fieldName, Object object){
        String firstLetter=fieldName.substring(0,1).toUpperCase();
        String getter = "get"+firstLetter+fieldName.substring(1);
        try {
            Method method = object.getClass().getMethod(getter);
            return method.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }
}

