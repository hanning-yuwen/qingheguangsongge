package com.qinghe.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public String handleDuplicateKey(DuplicateKeyException e) {
        // 把数据库约束错误转成前端可读信息，避免直接 500
        String msg = e.getMostSpecificCause() == null ? e.getMessage() : e.getMostSpecificCause().getMessage();
        return "FAIL:DUPLICATE_KEY:" + msg;
    }
}

