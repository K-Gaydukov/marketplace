package com.example.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {

    private Instant timestamp = Instant.now();
    private int status;
    private String code;
    private String message;
    private String path;

    public ErrorResponse(String message, String path) {
        this.message = message;
        this.path = path;

    }
}
