package com.rhythmiq.controlplaneservice.api.profile.get;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class GetProfileLambdaHandler extends BaseLambdaHandler {

    private final ProfileDao profileDao;

    public GetProfileLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public GetProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.profileDao = new ProfileDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String profileId = request.getPathParameters().get("profileId");

        try {
            var response = profileDao.getProfile(profileId);
            return createSuccessResponse(200, response);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return createErrorResponse(404, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to get profile.");
        }
    }
} 