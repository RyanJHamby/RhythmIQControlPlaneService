package com.rhythmiq.controlplaneservice;

import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import com.rhythmiq.controlplaneservice.model.*;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProfileResourceTest {
    @Mock
    private ProfileDao profileDao;

    private ProfileResource profileResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileResource = new ProfileResource(profileDao);
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

        CreateProfileResponse daoResponse = CreateProfileResponse.builder()
            .message("Profile created successfully")
            .build();

        when(profileDao.createProfile(any(CreateProfileRequest.class)))
            .thenReturn(daoResponse);

        // When
        Response response = profileResource.createProfile(request);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        CreateProfileResponse entity = (CreateProfileResponse) response.getEntity();
        assertNotNull(entity);
        assertEquals("Profile created successfully", entity.getMessage());
        verify(profileDao).createProfile(request);
    }

    @Test
    void createProfile_InvalidUsername() {
        // Given
        CreateProfileRequest request = CreateProfileRequest.builder()
            .email("test@example.com")
            .username("") // Empty username
            .firstName("Test")
            .lastName("User")
            .phoneNumber("1234567890")
            .build();

        // When
        Response response = profileResource.createProfile(request);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(profileDao, never()).createProfile(any());
    }

    @Test
    void getProfile_Success() {
        // Given
        String profileId = "test-id";
        GetProfileResponse daoResponse = GetProfileResponse.builder()
            .success(true)
            .message("Profile found")
            .build();

        when(profileDao.getProfile(profileId))
            .thenReturn(daoResponse);

        // When
        Response response = profileResource.getProfile(profileId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        GetProfileResponse entity = (GetProfileResponse) response.getEntity();
        assertNotNull(entity);
        assertTrue(entity.isSuccess());
        assertEquals("Profile found", entity.getMessage());
        verify(profileDao).getProfile(profileId);
    }

    @Test
    void getProfile_NotFound() {
        // Given
        String profileId = "non-existent-id";
        GetProfileResponse daoResponse = GetProfileResponse.builder()
            .success(false)
            .message("Profile not found")
            .build();

        when(profileDao.getProfile(profileId))
            .thenReturn(daoResponse);

        // When
        Response response = profileResource.getProfile(profileId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        GetProfileResponse entity = (GetProfileResponse) response.getEntity();
        assertFalse(entity.isSuccess());
        assertEquals("Profile not found", entity.getMessage());
        verify(profileDao).getProfile(profileId);
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

        UpdateProfileResponse daoResponse = UpdateProfileResponse.builder()
            .message("Profile updated successfully")
            .build();

        when(profileDao.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
            .thenReturn(daoResponse);

        // When
        Response response = profileResource.updateProfile(profileId, request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        UpdateProfileResponse entity = (UpdateProfileResponse) response.getEntity();
        assertNotNull(entity);
        assertEquals("Profile updated successfully", entity.getMessage());
        verify(profileDao).updateProfile(profileId, request);
    }

    @Test
    void updateProfile_InvalidUsername() {
        // Given
        String profileId = "test-id";
        UpdateProfileRequest request = UpdateProfileRequest.builder()
            .username("u") // Too short
            .build();

        // When
        Response response = profileResource.updateProfile(profileId, request);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verify(profileDao, never()).updateProfile(any(), any());
    }

    @Test
    void deleteProfile_Success() {
        // Given
        String profileId = "test-id";

        // When
        Response response = profileResource.deleteProfile(profileId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(profileDao).deleteProfile(profileId);
    }

    @Test
    void listProfiles_Success() {
        // Given
        ListProfilesResponse daoResponse = ListProfilesResponse.builder()
            .profiles(Arrays.asList())
            .build();

        when(profileDao.listProfiles())
            .thenReturn(daoResponse);

        // When
        Response response = profileResource.listProfiles();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ListProfilesResponse entity = (ListProfilesResponse) response.getEntity();
        assertNotNull(entity);
        verify(profileDao).listProfiles();
    }
} 