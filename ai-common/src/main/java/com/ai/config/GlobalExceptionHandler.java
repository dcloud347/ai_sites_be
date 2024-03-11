package com.ai.config;

import com.ai.util.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return Result.error().setMessage(message.toString());
    }
}
