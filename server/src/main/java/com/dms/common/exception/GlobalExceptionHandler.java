package com.dms.common.exception;

import cn.dev33.satoken.exception.SaTokenException;
import com.dms.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(Exception ex) {
        String msg = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException manve) {
            msg = manve.getBindingResult().getAllErrors().stream()
                    .map(e -> e.getDefaultMessage() == null ? e.toString() : e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof BindException be) {
            msg = be.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage() == null ? e.toString() : e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        log.warn("参数校验失败: {}", msg);  // 可选：记录警告
        return Result.fail(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("约束校验失败: {}", ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(SaTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleSaToken(SaTokenException ex) {
        log.error("Sa-Token 异常: {}", ex.getMessage(), ex);  // 关键：记录异常堆栈
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数异常: {}", ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleOther(Exception ex) {
        log.error("服务器未捕获异常: ", ex);  // 记录完整堆栈
        return Result.fail("服务器异常：" + ex.getMessage());
    }
}