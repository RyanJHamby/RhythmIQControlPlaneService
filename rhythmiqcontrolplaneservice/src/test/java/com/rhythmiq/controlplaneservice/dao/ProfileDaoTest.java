package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileDaoTest {
    @Mock
    private DynamoDbClient dynamoDbClient;

    private ProfileDao profileDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileDao = new ProfileDao(dynamoDbClient);
    }

    @Test
    void createProfile_Success() {
        // Given
        CreateProfileRequest request = CreateProfileRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("1234567890")
            .build();

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
            .thenReturn(PutItemResponse.builder().build());

        // When
        CreateProfileResponse response = profileDao.createProfile(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Profile created successfully", response.getMessage());
        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void createProfile_EmailAlreadyExists() {
        // Given
        CreateProfileRequest request = CreateProfileRequest.builder()
            .email("existing@example.com")
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("1234567890")
            .build();

        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
            .thenThrow(ConditionalCheckFailedException.builder().build());

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> profileDao.createProfile(request)
        );
        assertEquals("Email or username already exists", exception.getMessage());
    }

    @Test
    void getProfile_Success() {
        // Given
        String profileId = "test-id";
        Map<String, AttributeValue> item = Map.of(
            "profile_id", AttributeValue.builder().s(profileId).build(),
            "email", AttributeValue.builder().s("test@example.com").build(),
            "username", AttributeValue.builder().s("testuser").build(),
            "first_name", AttributeValue.builder().s("Test").build(),
            "last_name", AttributeValue.builder().s("User").build(),
            "phone_number", AttributeValue.builder().s("1234567890").build()
        );

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
            .thenReturn(GetItemResponse.builder().item(item).build());

        // When
        GetProfileResponse response = profileDao.getProfile(profileId);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getProfile());
        assertEquals(profileId, response.getProfile().getProfileId());
        assertEquals("test@example.com", response.getProfile().getEmail());
    }

    @Test
    void getProfile_NotFound() {
        // Given
        String profileId = "non-existent-id";
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
            .thenReturn(GetItemResponse.builder().build());

        // When
        GetProfileResponse response = profileDao.getProfile(profileId);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Profile not found", response.getMessage());
    }

    @Test
    void getProfileByEmail_Success() {
        // Given
        String email = "test@example.com";
        Map<String, AttributeValue> item = Map.of(
            "profile_id", AttributeValue.builder().s("test-id").build(),
            "email", AttributeValue.builder().s(email).build(),
            "username", AttributeValue.builder().s("testuser").build(),
            "first_name", AttributeValue.builder().s("Test").build(),
            "last_name", AttributeValue.builder().s("User").build(),
            "phone_number", AttributeValue.builder().s("1234567890").build()
        );

        when(dynamoDbClient.query(any(QueryRequest.class)))
            .thenReturn(QueryResponse.builder().items(List.of(item)).build());

        // When
        GetProfileResponse response = profileDao.getProfileByEmail(email);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getProfile());
        assertEquals(email, response.getProfile().getEmail());
    }

    @Test
    void getProfileByEmail_NotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(dynamoDbClient.query(any(QueryRequest.class)))
            .thenReturn(QueryResponse.builder().items(List.of()).build());

        // When
        GetProfileResponse response = profileDao.getProfileByEmail(email);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Profile not found", response.getMessage());
    }

    @Test
    void updateProfile_Success() {
        // Given
        String profileId = "test-id";
        UpdateProfileRequest request = UpdateProfileRequest.builder()
            .username("newusername")
            .firstName("NewFirst")
            .lastName("NewLast")
            .email("new@example.com")
            .phoneNumber("0987654321")
            .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
            .thenReturn(UpdateItemResponse.builder().build());

        // When
        UpdateProfileResponse response = profileDao.updateProfile(profileId, request);

        // Then
        assertNotNull(response);
        assertEquals("Profile updated successfully", response.getMessage());
        verify(dynamoDbClient).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void updateProfile_NotFound() {
        // Given
        String profileId = "non-existent-id";
        UpdateProfileRequest request = UpdateProfileRequest.builder()
            .username("newusername")
            .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
            .thenThrow(ConditionalCheckFailedException.builder().build());

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> profileDao.updateProfile(profileId, request)
        );
        assertEquals("Profile not found: " + profileId, exception.getMessage());
    }

    @Test
    void deleteProfile_Success() {
        // Given
        String profileId = "test-id";
        when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class)))
            .thenReturn(DeleteItemResponse.builder().build());

        // When/Then
        assertDoesNotThrow(() -> profileDao.deleteProfile(profileId));
        verify(dynamoDbClient).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    void deleteProfile_NotFound() {
        // Given
        String profileId = "non-existent-id";
        when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class)))
            .thenThrow(ConditionalCheckFailedException.builder().build());

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> profileDao.deleteProfile(profileId)
        );
        assertEquals("Profile not found: " + profileId, exception.getMessage());
    }

    @Test
    void listProfiles_Success() {
        // Given
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("profile_id", AttributeValue.builder().s("id1").build());
        item.put("username", AttributeValue.builder().s("user1").build());
        item.put("first_name", AttributeValue.builder().s("Test1").build());
        item.put("last_name", AttributeValue.builder().s("Last1").build());
        item.put("email", AttributeValue.builder().s("test1@example.com").build());

        ScanResponse scanResponse = ScanResponse.builder()
            .items(List.of(item))
            .lastEvaluatedKey(null)  // Explicitly set to null to indicate no more pages
            .build();

        when(dynamoDbClient.scan(any(ScanRequest.class)))
            .thenReturn(scanResponse);

        // When
        ListProfilesResponse response = profileDao.listProfiles();

        // Then
        assertNotNull(response);
        assertEquals(1, response.getProfiles().size());
        ProfileSummary profile = response.getProfiles().get(0);
        assertEquals("id1", profile.getProfileId());
        assertEquals("user1", profile.getUsername());
        assertEquals("Test1", profile.getFirstName());
        assertEquals("Last1", profile.getLastName());
        assertEquals("test1@example.com", profile.getEmail());

        verify(dynamoDbClient).scan(any(ScanRequest.class));
    }
}
