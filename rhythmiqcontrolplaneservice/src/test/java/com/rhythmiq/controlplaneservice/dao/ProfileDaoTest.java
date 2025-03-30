package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileDaoTest {

    private DynamoDbClient dynamoDbClient;
    private ProfileDao profileDao;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        profileDao = new ProfileDao(dynamoDbClient);
    }

    @Test
    void createProfile_Success() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .username("johndoe")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .password("password123")
                .build();

        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

        CreateProfileResponse response = profileDao.createProfile(request);

        assertNotNull(response.getId());
        assertEquals("Profile created successfully", response.getMessage());

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        Map<String, AttributeValue> item = captor.getValue().item();

        assertEquals("johndoe", item.get("username").s());
        assertEquals("john@example.com", item.get("email").s());
    }

    @Test
    void createProfile_EmailAlreadyExists() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .username("johndoe")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .password("password123")
                .build();

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> profileDao.createProfile(request));

        assertTrue(exception.getMessage().contains("Email or username already exists"));
    }
}
