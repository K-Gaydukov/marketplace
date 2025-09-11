package com.example.exception;

import com.example.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtValidationException(JwtValidationException e,
                                                                      HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "JWT_ERROR",
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e,
                                                                HttpServletRequest request) {
        return new ResponseEntity<>(
                new ErrorResponse(
                        "INTERNAL_ERROR",
                        e.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
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
                HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException e,
                                                               HttpServletRequest request) {
        String message = extractMessage(e.getResponseBodyAsString());
        String code = switch (e.getStatusCode().value()) {
            case 404 -> "NOT_FOUND";
            case 422 -> "UNPROCESSABLE_ENTITY";
            case 400 -> "VALIDATION_ERROR";
            default -> "PROXY_ERROR";
        };
        return new ResponseEntity<>(new ErrorResponse(
                code,
                message,
                request.getRequestURI(),
                (HttpStatus) e.getStatusCode()), e.getStatusCode());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e,
                                                                 HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "NOT_FOUND",
                e.getMessage() != null ? e.getMessage() : "Resource not found",
                request.getRequestURI(),
                HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e,
                                                                   HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "VALIDATION_ERROR",
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorResponse(
                "VALIDATION_ERROR",
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        String message = "Data integrity violation: " + e.getMostSpecificCause().getMessage();
        return new ResponseEntity<>(new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    private String extractMessage(String body) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(body, Map.class);
            return (String) jsonMap.getOrDefault("message", body);
        } catch (IOException ex) {
            return body;
        }
    }
}
