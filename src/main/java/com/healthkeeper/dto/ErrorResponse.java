package com.healthkeeper.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String path;
    private String timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.error = "Internal Server Error";
        this.status = 500;
        this.path = "/api/products";
        this.timestamp = java.time.Instant.now().toString();
    }
} 