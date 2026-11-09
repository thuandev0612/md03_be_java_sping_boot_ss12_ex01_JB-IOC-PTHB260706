package com.example.baitapsession11.exception;

import com.example.baitapsession11.dto.ApiResponseError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct().sorted().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiResponseError(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseError> handleInvalidJson(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ApiResponseError(400, "Dữ liệu JSON không hợp lệ"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseError> handleException(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse error
                && error.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders())
                    .body(new ApiResponseError(error.getStatusCode().value(), "Yêu cầu không hợp lệ"));
        }
        log.error("Lỗi hệ thống xảy ra: ", e);
        return ResponseEntity.internalServerError()
                .body(new ApiResponseError(500, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau"));
    }
}
