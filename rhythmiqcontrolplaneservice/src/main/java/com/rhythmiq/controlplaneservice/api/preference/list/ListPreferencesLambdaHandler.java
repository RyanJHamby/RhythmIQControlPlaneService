package com.rhythmiq.controlplaneservice.api.preference.list;

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
import java.util.List;

@Log4j2
public class ListPreferencesLambdaHandler extends BaseLambdaHandler {

    private final PreferenceDao preferenceDao;

    public ListPreferencesLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public ListPreferencesLambdaHandler(DynamoDbClient dynamoDbClient) {
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

        try {
            List<Preference> preferences = preferenceDao.listPreferences(profileId);
            return createSuccessResponse(200, preferences);
        } catch (Exception e) {
            log.error("Failed to list preferences", e);
            return createErrorResponse(500, "Failed to list preferences");
        }
    }
}
