package com.example.baitapsession12.exception;

import com.example.baitapsession12.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(ApiException e) {
        log.error("Lỗi nghiệp vụ: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatus()).body(new ApiResponse<>("FAIL", e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        log.error("Dữ liệu không hợp lệ: {}", errors, e);
        return ResponseEntity.badRequest().body(new ApiResponse<>("FAIL", "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJson(HttpMessageNotReadableException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException business) {
                log.error("Yêu cầu JSON bị từ chối: {}", business.getMessage(), e);
                return ResponseEntity.status(business.getStatus())
                        .body(new ApiResponse<>("FAIL", business.getMessage(), null));
            }
        }
        log.error("Dữ liệu JSON không hợp lệ", e);
        return ResponseEntity.badRequest().body(new ApiResponse<>("FAIL", "Dữ liệu JSON không hợp lệ", null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameter(MethodArgumentTypeMismatchException e) {
        log.error("Tham số không hợp lệ", e);
        return ResponseEntity.badRequest().body(new ApiResponse<>("FAIL", "Tham số không hợp lệ", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
        log.error("Lỗi xử lý yêu cầu: ", e);
        if (e instanceof org.springframework.web.ErrorResponse response) {
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
                    .body(new ApiResponse<>("FAIL", "Yêu cầu không hợp lệ", null));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("FAIL", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau", null));
    }
}
