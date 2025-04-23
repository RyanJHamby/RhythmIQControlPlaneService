package com.rhythmiq.controlplaneservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpdateProfileRequestTest {

    @Test
    void testBuilder() {
        // Arrange & Act
        UpdateProfileRequest request = UpdateProfileRequest.builder()
            .username("johndoe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("123-456-7890")
            .password("newPassword123")
            .build();

        // Assert
        assertEquals("johndoe", request.getUsername());
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john.doe@example.com", request.getEmail());
        assertEquals("123-456-7890", request.getPhoneNumber());
        assertEquals("newPassword123", request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act
        request.setUsername("johndoe")
            .setFirstName("John")
            .setLastName("Doe")
            .setEmail("john.doe@example.com")
            .setPhoneNumber("123-456-7890")
            .setPassword("newPassword123");

        // Assert
        assertEquals("johndoe", request.getUsername());
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john.doe@example.com", request.getEmail());
        assertEquals("123-456-7890", request.getPhoneNumber());
        assertEquals("newPassword123", request.getPassword());
    }

    @Test
    void testPartialUpdate() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();

        // Act
        request.setFirstName("John")
            .setLastName("Doe");

        // Assert
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getPhoneNumber());
        assertNull(request.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        UpdateProfileRequest request1 = new UpdateProfileRequest()
            .setFirstName("John")
            .setLastName("Doe");

        UpdateProfileRequest request2 = new UpdateProfileRequest()
            .setFirstName("John")
            .setLastName("Doe");

        UpdateProfileRequest request3 = new UpdateProfileRequest()
            .setFirstName("Jane")
            .setLastName("Doe");

        // Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }
}
