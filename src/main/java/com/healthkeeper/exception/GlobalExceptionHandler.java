/**
 * AI-generated-content
 * tool: Claude 
 * version: 3.7 Sonnet
 * usage: I used the prompt 
 * "Help me implement the functions of the back-end" 
 * and directly copied the code from its response.
 */
package com.healthkeeper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        // 记录异常详细信息到控制台
        System.err.println("全局异常处理器捕获到异常:");
        ex.printStackTrace();

        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("success", false);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 对于不同类型的异常，可能需要返回不同的状态码
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // 记录详细的错误信息，包括异常类型和请求类型
        System.err.println("异常类型: " + ex.getClass().getName());
        System.err.println("请求类型: " + request.getDescription(true));

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        System.err.println("运行时异常处理器捕获到异常:");
        ex.printStackTrace();

        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("success", false);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 对于运行时异常，通常使用400错误
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(body, status);
    }
}
