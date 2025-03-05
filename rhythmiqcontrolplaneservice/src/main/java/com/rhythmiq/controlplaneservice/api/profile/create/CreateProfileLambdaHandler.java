package com.rhythmiq.controlplaneservice.api.profile.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.rhthymiq.controlplaneservice.model.CreateProfileRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.StringUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CreateProfileLambdaHandler implements RequestHandler<CreateProfileRequest, String> {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Profiles";

    @Inject
    public CreateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public String handleRequest(CreateProfileRequest request, Context context) {
        context.getLogger().log("Received request for user: " + request.getName());

        if (StringUtils.isBlank(request.getName())) {
            return "Error: Name is required.";
        }
        if (StringUtils.isBlank(request.getEmail())) {
            return "Error: Email is required.";
        }

        // Store in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("name", AttributeValue.builder().s(request.getName()).build());
        item.put("email", AttributeValue.builder().s(request.getEmail()).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            return "Profile successfully created for user: " + request.getName();
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return "Error: Failed to create profile.";
        }
    }
}
