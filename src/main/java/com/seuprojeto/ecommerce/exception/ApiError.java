package com.seuprojeto.ecommerce.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        List<String> details
) {

    public ApiError(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }

    public ApiError(int status, String error, String message, List<String> details) {
        this(status, error, message, LocalDateTime.now(), details);
    }
}