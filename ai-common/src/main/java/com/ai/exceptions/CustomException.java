package com.ai.exceptions;

import com.ai.util.ResultCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    ResultCode code;
    public CustomException(String message, ResultCode code) {
        super(message);
        this.code = code;
    }
    public CustomException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST;
    }

}
