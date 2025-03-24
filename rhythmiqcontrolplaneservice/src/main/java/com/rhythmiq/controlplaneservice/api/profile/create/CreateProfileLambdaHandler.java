package com.rhythmiq.controlplaneservice.api.profile.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.StringUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CreateProfileLambdaHandler extends BaseLambdaHandler {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Profiles";

    @Inject
    public CreateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        log.info("Received request: {}", request.getBody());

        CreateProfileRequest profileRequest;
        try {
            profileRequest = objectMapper.readValue(request.getBody(), CreateProfileRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request format.");
        }

        if (StringUtils.isBlank(profileRequest.getName())) {
            return createErrorResponse(400, "Request must include a name.");
        }
        if (StringUtils.isBlank(profileRequest.getEmail())) {
            return createErrorResponse(400, "Request must include an email.");
        }

        // Store in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("username", AttributeValue.builder().s(profileRequest.getUsername()).build());
        item.put("name", AttributeValue.builder().s(profileRequest.getName()).build());
        item.put("email", AttributeValue.builder().s(profileRequest.getEmail()).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            log.info("Profile created successfully: {}", profileRequest.getEmail());

            // Create success response
            CreateProfileResponse response = new CreateProfileResponse()
                    .name(profileRequest.getName());
            return createSuccessResponse(200, response);

        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to create profile.");
        }
    }
}
