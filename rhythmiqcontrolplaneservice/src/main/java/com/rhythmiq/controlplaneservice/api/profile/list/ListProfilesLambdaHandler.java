package com.rhythmiq.controlplaneservice.api.profile.list;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class ListProfilesLambdaHandler extends BaseLambdaHandler {

    private final ProfileDao profileDao;

    public ListProfilesLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public ListProfilesLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.profileDao = new ProfileDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            var response = profileDao.listProfiles();
            return createSuccessResponse(200, response);
        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to list profiles.");
        }
    }
} 