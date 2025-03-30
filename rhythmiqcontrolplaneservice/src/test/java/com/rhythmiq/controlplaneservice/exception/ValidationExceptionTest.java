package com.rhythmiq.controlplaneservice.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
    @Test
    void testDefaultConstructor() {
        // When
        ValidationException exception = new ValidationException();

        // Then
        assertNotNull(exception);
        assertEquals("Validation failed", exception.getMessage());
        assertTrue(exception.getErrors().isEmpty());
    }

    @Test
    void testMessageAndErrorsConstructor() {
        // Given
        String message = "Custom validation error";
        Map<String, Integer> errors = Map.of(
            "field1", 1,
            "field2", 2
        );

        // When
        ValidationException exception = new ValidationException(message, errors);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(errors, exception.getErrors());
        assertEquals(2, exception.getErrors().size());
        assertEquals(1, exception.getErrors().get("field1"));
        assertEquals(2, exception.getErrors().get("field2"));
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Map<String, Integer> errors1 = Map.of("field1", 1);
        Map<String, Integer> errors2 = Map.of("field1", 1);
        ValidationException exception1 = new ValidationException("error", errors1);
        ValidationException exception2 = new ValidationException("error", errors2);
        ValidationException differentException = new ValidationException("different", Map.of("field2", 2));

        // Then
        assertEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals(exception1.getErrors(), exception2.getErrors());
        assertNotEquals(exception1.getMessage(), differentException.getMessage());
        assertNotEquals(exception1.getErrors(), differentException.getErrors());
    }

    @Test
    void testToString() {
        // Given
        Map<String, Integer> errors = Map.of("field1", 1);
        ValidationException exception = new ValidationException("error", errors);

        // When
        String toString = exception.toString();

        // Then
        assertTrue(toString.contains("error"));
        assertTrue(toString.contains("errors=" + errors));
    }
} 