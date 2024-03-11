package com.ai.util;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果
 * @author 刘晨
 */
@Data
@Accessors(chain = true)
@ToString
public class Result {
    //操作是否成功
    private Boolean isSuccess;
    //状态码
    private Integer status;
    //消息
    private String message;
    //返回操作数据
    private Map<String, Object> data;

    public static Result success() {
        return new Result().setIsSuccess(true)
                .setStatus(ResultCode.SUCCESS)
                .setMessage("操作成功")
                .setData(new HashMap<>());
    }

    public static Result error() {
        return new Result().setIsSuccess(false)
                .setStatus(ResultCode.ERROR)
                .setMessage("操作失败")
                .setData(new HashMap<>());
    }

    public Result setData(Map<String,Object> data){
        this.data=data;
        return this;
    }

    public Result setData(String key,Object value){
        this.data.put(key,value);
        return this;
    }
}
