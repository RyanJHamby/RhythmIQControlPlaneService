package com.rhythmiq.controlplaneservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreateProfileRequestTest {
    @Test
    void testBuilder() {
        // Given
        String username = "testuser";
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        String phoneNumber = "1234567890";

        // When
        CreateProfileRequest request = CreateProfileRequest.builder()
            .username(username)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .phoneNumber(phoneNumber)
            .build();

        // Then
        assertEquals(username, request.getUsername());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(email, request.getEmail());
        assertEquals(phoneNumber, request.getPhoneNumber());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        CreateProfileRequest request = new CreateProfileRequest();

        // Then
        assertNull(request.getUsername());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEmail());
        assertNull(request.getPhoneNumber());
    }

    @Test
    void testSetters() {
        // Given
        CreateProfileRequest request = new CreateProfileRequest();
        String username = "testuser";
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        String phoneNumber = "1234567890";

        // When
        request.setUsername(username);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPhoneNumber(phoneNumber);

        // Then
        assertEquals(username, request.getUsername());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
        assertEquals(email, request.getEmail());
        assertEquals(phoneNumber, request.getPhoneNumber());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        CreateProfileRequest request1 = CreateProfileRequest.builder()
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .phoneNumber("1234567890")
            .build();

        CreateProfileRequest request2 = CreateProfileRequest.builder()
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .phoneNumber("1234567890")
            .build();

        CreateProfileRequest differentRequest = CreateProfileRequest.builder()
            .username("different")
            .email("different@example.com")
            .build();

        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, differentRequest);
        assertNotEquals(request1.hashCode(), differentRequest.hashCode());
    }

    @Test
    void testToString() {
        // Given
        CreateProfileRequest request = CreateProfileRequest.builder()
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .phoneNumber("1234567890")
            .build();

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("firstName=Test"));
        assertTrue(toString.contains("lastName=User"));
        assertTrue(toString.contains("email=test@example.com"));
        assertTrue(toString.contains("phoneNumber=1234567890"));
    }
} 