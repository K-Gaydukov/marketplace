package com.example.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String path;

    public ErrorResponse(String code, String message, String path, HttpStatus httpStatus) {
        timestamp = LocalDateTime.now().toString();
        this.status = httpStatus.value();
        this.code = code;
        this.message = message;
        this.path = path;
    }
}
