package com.rhythmiq.controlplaneservice.api.preference.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.PreferenceDao;
import com.rhythmiq.controlplaneservice.model.Preference;
import com.rhythmiq.controlplaneservice.model.CreatePreferenceRequest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class CreatePreferenceLambdaHandler extends BaseLambdaHandler {

    private final PreferenceDao preferenceDao;

    public CreatePreferenceLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public CreatePreferenceLambdaHandler(DynamoDbClient dynamoDbClient) {
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
        if (profileId == null || profileId.trim().isEmpty()) {
            return createErrorResponse(400, "Profile ID is required");
        }

        CreatePreferenceRequest createRequest;
        try {
            createRequest = objectMapper.readValue(request.getBody(), CreatePreferenceRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request body");
        }

        // Create preference from request
        Preference preference = Preference.builder()
                .profileId(profileId)
                .preferenceId(java.util.UUID.randomUUID().toString()) // Generate unique ID
                .type(createRequest.getType())
                .value(createRequest.getValue())
                .index(createRequest.getIndex())
                .weight(createRequest.getWeight())
                .isUserSet(true)
                .build();

        try {
            preferenceDao.createPreference(preference);
            return createSuccessResponse(200, preference);
        } catch (Exception e) {
            log.error("Failed to create preference", e);
            return createErrorResponse(500, "Failed to create preference");
        }
    }
}
