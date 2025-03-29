package com.rhythmiq.controlplaneservice.api.profile.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class CreateProfileLambdaHandler extends BaseLambdaHandler {

    private final ProfileDao profileDao;

    public CreateProfileLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public CreateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.profileDao = new ProfileDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        CreateProfileRequest profileRequest;
        try {
            profileRequest = objectMapper.readValue(request.getBody(), CreateProfileRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request format.");
        }

        try {
            CreateProfileResponse response = profileDao.createProfile(profileRequest);
            return createSuccessResponse(201, response);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return createErrorResponse(409, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to create profile.");
        }
    }
}
