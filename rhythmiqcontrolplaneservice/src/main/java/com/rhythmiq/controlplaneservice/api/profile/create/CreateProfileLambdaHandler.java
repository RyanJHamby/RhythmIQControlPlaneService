package com.rhythmiq.controlplaneservice.api.profile.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.StringUtils;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class CreateProfileLambdaHandler extends BaseLambdaHandler {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Profiles";

    // Public no-argument constructor, required by AWS Lambda
    public CreateProfileLambdaHandler() {
        this.dynamoDbClient = DynamoDbClient.create();  // DynamoDbClient is instantiated directly here
    }

    @Inject
    public CreateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("Received request: " + request.getBody());

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

        String profileId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // Store in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("profile_id", AttributeValue.builder().s(profileId).build());
        item.put("email", AttributeValue.builder().s(profileRequest.getEmail()).build());
        item.put("name", AttributeValue.builder().s(profileRequest.getName()).build());
        item.put("created_at", AttributeValue.builder().s(now.toString()).build());
        item.put("updated_at", AttributeValue.builder().s(now.toString()).build());
        // Add username as a key since it's required by the DynamoDB table schema
        item.put("username", AttributeValue.builder().s(profileRequest.getEmail()).build());

        try {
            System.out.println("DEBUG: Lambda is running...");

            // Add condition to ensure email uniqueness
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .conditionExpression("attribute_not_exists(email)")
                    .build();

            log.info("putItemRequest: {}", putItemRequest);

            dynamoDbClient.putItem(putItemRequest);
            log.info("Profile created successfully with ID: {}", profileId);

            // Create success response with profile ID
            CreateProfileResponse response = new CreateProfileResponse()
                    .id(profileId)
                    .name(profileRequest.getName());
            return createSuccessResponse(201, response);

        } catch (ConditionalCheckFailedException e) {
            log.error("Email already exists: {}", profileRequest.getEmail());
            return createErrorResponse(409, "Email address already in use.");
        } catch (Exception e) {
            log.error("Error processing request", e);
            return createErrorResponse(500, "Failed to create profile :/.");
        }
    }
}
