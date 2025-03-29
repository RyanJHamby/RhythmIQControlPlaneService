package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.CreateProfileRequest;
import com.rhythmiq.controlplaneservice.model.CreateProfileResponse;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ProfileDao {
    private static final String TABLE_NAME = "Profiles";
    private final DynamoDbClient dynamoDbClient;

    public CreateProfileResponse createProfile(CreateProfileRequest request) {
        String profileId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("profile_id", AttributeValue.builder().s(profileId).build());
        item.put("email", AttributeValue.builder().s(request.getEmail()).build());
        item.put("name", AttributeValue.builder().s(request.getName()).build());
        item.put("created_at", AttributeValue.builder().s(now.toString()).build());
        item.put("updated_at", AttributeValue.builder().s(now.toString()).build());
        item.put("username", AttributeValue.builder().s(request.getEmail()).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .conditionExpression("attribute_not_exists(email)")
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            return new CreateProfileResponse().id(profileId).name(request.getName());
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalStateException("Email already exists: " + request.getEmail(), e);
        }
    }
}
