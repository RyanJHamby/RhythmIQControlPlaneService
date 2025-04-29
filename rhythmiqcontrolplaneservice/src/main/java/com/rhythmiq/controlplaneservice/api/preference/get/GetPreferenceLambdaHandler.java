package com.rhythmiq.controlplaneservice.api.preference.get;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.PreferenceDao;
import com.rhythmiq.controlplaneservice.model.Preference;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;
import java.util.Optional;

@Log4j2
public class GetPreferenceLambdaHandler extends BaseLambdaHandler {

    private final PreferenceDao preferenceDao;

    public GetPreferenceLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public GetPreferenceLambdaHandler(DynamoDbClient dynamoDbClient) {
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

        try {
            Optional<Preference> preference = preferenceDao.getPreference(profileId, preferenceId);
            if (preference.isPresent()) {
                return createSuccessResponse(200, preference.get());
            } else {
                return createErrorResponse(404, "Preference not found");
            }
        } catch (Exception e) {
            log.error("Failed to get preference", e);
            return createErrorResponse(500, "Failed to get preference");
        }
    }
}
