package com.fadeway32.postadmin.web;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> notLogin(NotLoginException ex) {
        return Result.fail(401, "not login");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> invalid(MethodArgumentNotValidException ex) {
        return Result.fail(400, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> badRequest(IllegalArgumentException ex) {
        return Result.fail(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> error(Exception ex) {
        return Result.fail(500, ex.getMessage());
    }
}
