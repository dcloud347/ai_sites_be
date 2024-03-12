package com.ai.util;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果
 * @author 刘晨
 */
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1175720458347412087L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 相关数据
     */
    private T data;

    /**
     * 无参响应成功
     */
    public static <T> Result<T> success() {
        return new Result<T>();
    }

    /**
     * 成功,默认状态码,返回消息,自定义返回数据
     *
     * @param data 自定义返回数据
     * @param <T>  返回类泛型,不能为String
     * @return 通用返回Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    /**
     * 成功,默认状态码,自定义返回消息,无返回数据
     *
     * @param msg 自定义返回消息
     * @param <T> 返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> success(String msg) {
        return new Result<T>(msg);
    }

    /**
     * 成功,默认状态码,自定义返回消息,返回数据
     *
     * @param msg  自定义返回消息
     * @param data 自定义返回数据
     * @param <T>  返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<T>(msg, data);
    }

    /**
     * 失败,默认状态码,返回消息,无返回数据
     *
     * @param <T> 返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> error() {
        return new Result<T>(ResultCode.ERROR);
    }

    /**
     * 失败,默认状态码,自定义返回消息,无返回数据
     *
     * @param <T> 返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> error(String msg) {
        return new Result<T>(ResultCode.ERROR.getCode(), msg);
    }

    /**
     * 失败,自定义状态码,返回消息,无返回数据
     *
     * @param code 自定义状态码
     * @param msg  自定义返回消息
     * @param <T>  返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<T>(code, msg);
    }

    /**
     * 失败,使用CodeMsg状态码,返回消息,无返回数据
     *
     * @param resultCode CodeMsg,参数如下:
     *                   <p> code 状态码
     *                   <p> msg  返回消息
     * @param <T>        返回类泛型
     * @return 通用返回Result
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<T>(resultCode);
    }

    /**
     * 成功构造器,无返回数据
     */
    private Result() {
        this(ResultCode.SUCCESS);
    }

    /**
     * 成功构造器,自定义返回数据
     *
     * @param data 返回数据
     */
    private Result(T data) {
        this(ResultCode.SUCCESS, data);
    }

    /**
     * 成功构造器,自定义返回消息,无返回数据
     *
     * @param msg 返回消息
     */
    public Result(String msg) {
        this(ResultCode.SUCCESS.getCode(), msg);
    }

    /**
     * 成功构造器,自定义返回信息,返回数据
     *
     * @param msg  返回信息
     * @param data 返回数据
     */
    private Result(String msg, T data) {
        this(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 构造器,自定义状态码,返回消息
     *
     * @param code 状态码
     * @param msg  返回消息
     */
    private Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 构造器,自定义状态码,返回消息,返回数据
     *
     * @param code 状态码
     * @param msg  返回消息
     * @param data 返回数据
     */
    private Result(int code, String msg, T data) {
        this(code, msg);
        this.data = data;
    }

    /**
     * 构造器,使用CodeMsg状态码与返回信息
     *
     * @param resultCode CodeMsg,参数如下:
     *                   <p> code 状态码
     *                   <p> msg  返回消息
     */
    private Result(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg());
    }

    /**
     * 构造器,使用CodeMsg状态码与返回信息,自定义返回数据
     *
     * @param resultCode CodeMsg,参数如下:
     *                   <p> code 状态码
     *                   <p> msg  返回消息
     * @param data       返回数据
     */
    private Result(ResultCode resultCode, T data) {
        this(resultCode);
        this.data = data;
    }
}

