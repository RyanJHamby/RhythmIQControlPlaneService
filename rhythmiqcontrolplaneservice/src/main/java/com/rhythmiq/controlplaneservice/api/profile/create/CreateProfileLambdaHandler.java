package com.rhythmiq.controlplaneservice.api.profile.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.StringUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CreateProfileLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "Profiles";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public CreateProfileLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Received request: " + request.getBody());

        CreateProfileRequest profileRequest = null;
        try {
            profileRequest = objectMapper.readValue(request.getBody(), CreateProfileRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isBlank(profileRequest.getName())) {
            return createErrorResponse(400, "Request must include a name.");
        }
        if (StringUtils.isBlank(profileRequest.getEmail())) {
            return createErrorResponse(400, "Request must include an email.");
        }

        // Store in DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("name", AttributeValue.builder().s(profileRequest.getName()).build());
        item.put("email", AttributeValue.builder().s(profileRequest.getEmail()).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

            // Create success response
            CreateProfileResponse response = new CreateProfileResponse()
                    .name(profileRequest.getName());
            return createSuccessResponse(200, response);

        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createErrorResponse(500, "Failed to create profile.");
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object responseBody) throws Exception {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(getHeaders())
                .withBody(objectMapper.writeValueAsString(responseBody));
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getHeaders())
                    .withBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(getHeaders())
                    .withBody("{\"error\": \"Internal Server Error\"}");
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
