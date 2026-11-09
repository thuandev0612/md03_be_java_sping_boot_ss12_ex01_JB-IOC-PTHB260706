package com.example.baitapsession11.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponseError {
    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    public ApiResponseError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
