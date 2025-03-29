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
        CreateProfileRequest request = new CreateProfileRequest().name("John Doe").email("john@example.com");

        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

        CreateProfileResponse response = profileDao.createProfile(request);

        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        Map<String, AttributeValue> item = captor.getValue().item();

        assertEquals("John Doe", item.get("name").s());
        assertEquals("john@example.com", item.get("email").s());
    }

    @Test
    void createProfile_EmailAlreadyExists() {
        CreateProfileRequest request = new CreateProfileRequest().name("John Doe").email("john@example.com");

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> profileDao.createProfile(request));

        assertTrue(exception.getMessage().contains("Email already exists"));
    }
}
