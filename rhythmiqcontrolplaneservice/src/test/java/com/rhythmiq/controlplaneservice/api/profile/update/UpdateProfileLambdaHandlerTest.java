package com.rhythmiq.controlplaneservice.api.profile.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import com.rhythmiq.controlplaneservice.model.UpdateProfileRequest;
import com.rhythmiq.controlplaneservice.model.UpdateProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UpdateProfileLambdaHandlerTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private Context context;

    @Mock
    private ProfileDao profileDao;

    private UpdateProfileLambdaHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new UpdateProfileLambdaHandler(dynamoDbClient) {
            @Override
            protected ProfileDao createProfileDao(DynamoDbClient dynamoDbClient) {
                return profileDao;
            }
        };
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleRequest_ValidUpdate_ReturnsSuccess() throws Exception {
        // Arrange
        String profileId = "test-profile-id";
        UpdateProfileRequest updateRequest = new UpdateProfileRequest()
            .setFirstName("John")
            .setLastName("Doe")
            .setPhoneNumber("123-456-7890");

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("profileId", profileId);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
            .withPathParameters(pathParameters)
            .withBody(objectMapper.writeValueAsString(updateRequest));

        UpdateProfileResponse expectedResponse = new UpdateProfileResponse();
        expectedResponse.setMessage("Profile updated successfully");

        when(profileDao.updateProfile(any(), any()))
            .thenReturn(expectedResponse);

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        UpdateProfileResponse actualResponse = objectMapper.readValue(response.getBody(), UpdateProfileResponse.class);
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
    }

    @Test
    void handleRequest_InvalidJson_ReturnsBadRequest() {
        // Arrange
        String profileId = "test-profile-id";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("profileId", profileId);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
            .withPathParameters(pathParameters)
            .withBody("invalid json");

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid request format"));
    }

    @Test
    void handleRequest_ProfileNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String profileId = "non-existent-id";
        UpdateProfileRequest updateRequest = new UpdateProfileRequest()
            .setFirstName("John")
            .setLastName("Doe");

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("profileId", profileId);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
            .withPathParameters(pathParameters)
            .withBody(objectMapper.writeValueAsString(updateRequest));

        when(profileDao.updateProfile(any(), any()))
            .thenThrow(new IllegalStateException("Profile not found"));

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("Profile not found"));
    }

    @Test
    void handleRequest_MissingProfileId_ReturnsBadRequest() {
        // Arrange
        UpdateProfileRequest updateRequest = new UpdateProfileRequest()
            .setFirstName("John")
            .setLastName("Doe");

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
            .withPathParameters(null)
            .withBody("{}");

        // Act
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
    }
}
