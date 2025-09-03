package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtValidationException(JwtValidationException e,
                                                                      HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "JWT_ERROR",
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e,
                                                                HttpServletRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        "INTERNAL_ERROR",
                        e.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
                                                                   HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
                ), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException e, HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "PROXY_ERROR",
                e.getMessage(),
                request.getRequestURI(),
                (HttpStatus) e.getStatusCode()), e.getStatusCode());
    }
}
