package com.example.baitapsession12.dto;

public record ApiResponse<T>(String status, String message, T data) {
}
