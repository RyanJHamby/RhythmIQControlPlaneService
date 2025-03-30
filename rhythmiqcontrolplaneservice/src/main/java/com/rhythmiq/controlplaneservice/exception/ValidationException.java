package com.rhythmiq.controlplaneservice.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {
    private final Map<String, Integer> errors;

    public ValidationException() {
        super("Validation failed");
        this.errors = Map.of();
    }

    public ValidationException(String message, Map<String, Integer> errors) {
        super(message);
        this.errors = errors;
    }
} 