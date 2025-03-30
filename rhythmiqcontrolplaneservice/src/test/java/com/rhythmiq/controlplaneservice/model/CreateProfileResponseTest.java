package com.rhythmiq.controlplaneservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreateProfileResponseTest {
    @Test
    void testBuilder() {
        // Given
        String id = "profile-123";
        String message = "Profile created successfully";

        // When
        CreateProfileResponse response = CreateProfileResponse.builder()
            .id(id)
            .message(message)
            .build();

        // Then
        assertEquals(id, response.getId());
        assertEquals(message, response.getMessage());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        CreateProfileResponse response = new CreateProfileResponse();

        // Then
        assertNull(response.getId());
        assertNull(response.getMessage());
    }

    @Test
    void testSetters() {
        // Given
        CreateProfileResponse response = new CreateProfileResponse();
        String id = "profile-123";
        String message = "Profile created successfully";

        // When
        response.setId(id);
        response.setMessage(message);

        // Then
        assertEquals(id, response.getId());
        assertEquals(message, response.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        CreateProfileResponse response1 = CreateProfileResponse.builder()
            .id("profile-123")
            .message("Profile created successfully")
            .build();

        CreateProfileResponse response2 = CreateProfileResponse.builder()
            .id("profile-123")
            .message("Profile created successfully")
            .build();

        CreateProfileResponse differentResponse = CreateProfileResponse.builder()
            .id("profile-456")
            .message("Different message")
            .build();

        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, differentResponse);
        assertNotEquals(response1.hashCode(), differentResponse.hashCode());
    }

    @Test
    void testToString() {
        // Given
        CreateProfileResponse response = CreateProfileResponse.builder()
            .id("profile-123")
            .message("Profile created successfully")
            .build();

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("id=profile-123"));
        assertTrue(toString.contains("message=Profile created successfully"));
    }
} 