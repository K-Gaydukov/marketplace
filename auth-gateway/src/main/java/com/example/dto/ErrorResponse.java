package com.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private String error;
    private String timestamp;
    private String path;

    public ErrorResponse(String error, String path) {
        this.error = error;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;

    }
}
