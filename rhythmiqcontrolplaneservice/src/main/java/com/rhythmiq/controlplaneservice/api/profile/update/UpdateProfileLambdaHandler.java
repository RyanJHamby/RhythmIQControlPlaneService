package com.rhythmiq.controlplaneservice.api.profile.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import com.rhythmiq.controlplaneservice.model.UpdateProfileRequest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class UpdateProfileLambdaHandler extends BaseLambdaHandler {

    private final ProfileDao profileDao;

    public UpdateProfileLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public UpdateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.profileDao = createProfileDao(dynamoDbClient);
    }

    protected ProfileDao getProfileDao() {
        return profileDao;
    }

    protected ProfileDao createProfileDao(DynamoDbClient dynamoDbClient) {
        return new ProfileDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String profileId = request.getPathParameters().get("profileId");

        UpdateProfileRequest updateRequest;
        try {
            updateRequest = objectMapper.readValue(request.getBody(), UpdateProfileRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request format.");
        }

        try {
            var response = profileDao.updateProfile(profileId, updateRequest);
            return createSuccessResponse(200, response);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return createErrorResponse(404, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to update profile.");
        }
    }
} 