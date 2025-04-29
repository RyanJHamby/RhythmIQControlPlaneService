package com.rhythmiq.controlplaneservice.api.preference.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.PreferenceDao;
import com.rhythmiq.controlplaneservice.model.Preference;
import com.rhythmiq.controlplaneservice.model.UpdatePreferenceRequest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;
import java.util.Optional;

@Log4j2
public class UpdatePreferenceLambdaHandler extends BaseLambdaHandler {

    private final PreferenceDao preferenceDao;

    public UpdatePreferenceLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public UpdatePreferenceLambdaHandler(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.preferenceDao = new PreferenceDao(enhancedClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        if (request.getPathParameters() == null) {
            return createErrorResponse(400, "Missing path parameters");
        }

        String profileId = request.getPathParameters().get("profileId");
        String preferenceId = request.getPathParameters().get("preferenceId");

        if (profileId == null || profileId.trim().isEmpty()) {
            return createErrorResponse(400, "Profile ID is required");
        }
        if (preferenceId == null || preferenceId.trim().isEmpty()) {
            return createErrorResponse(400, "Preference ID is required");
        }

        UpdatePreferenceRequest updateRequest;
        try {
            updateRequest = objectMapper.readValue(request.getBody(), UpdatePreferenceRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request body");
        }

        // Get existing preference
        Optional<Preference> existingPreference = preferenceDao.getPreference(profileId, preferenceId);
        if (existingPreference.isEmpty()) {
            return createErrorResponse(404, "Preference not found");
        }

        // Update preference with new values
        Preference preference = existingPreference.get();
        if (updateRequest.getValue() != null) {
            preference.setValue(updateRequest.getValue());
        }
        if (updateRequest.getIndex() != null) {
            preference.setIndex(updateRequest.getIndex());
        }
        if (updateRequest.getWeight() != null) {
            preference.setWeight(updateRequest.getWeight());
        }

        try {
            preferenceDao.updatePreference(preference);
            return createSuccessResponse(200, preference);
        } catch (Exception e) {
            log.error("Failed to update preference", e);
            return createErrorResponse(500, "Failed to update preference");
        }
    }
}
