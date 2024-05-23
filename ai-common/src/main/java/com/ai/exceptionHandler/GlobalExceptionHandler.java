package com.ai.exceptionHandler;
import com.ai.util.Result;
import com.ai.util.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Result<String>> handleMultipartException(MultipartException ex) {
        return ResponseEntity.status(ResultCode.BAD_REQUEST.getCode()).body(Result.error("当前请求不是Multipart请求"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result exceptionHandler(MethodArgumentNotValidException methodArgumentNotValidException ) throws Exception {
        List<FieldError> fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (int i=0; i<fieldErrors.size(); i++) {
            message.append(fieldErrors.get(i).getDefaultMessage());
            if (i < fieldErrors.size()-1) {
                message.append(";");
            }
        }
        return Result.error(message.toString());
    }
}

