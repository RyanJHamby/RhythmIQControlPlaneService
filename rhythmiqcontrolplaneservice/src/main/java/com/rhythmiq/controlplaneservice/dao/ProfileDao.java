package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.*;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class ProfileDao {
    private static final String TABLE_NAME = "Profiles";
    private final DynamoDbClient dynamoDbClient;

    @Inject
    public ProfileDao(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public CreateProfileResponse createProfile(CreateProfileRequest request) {
        String profileId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("profile_id", AttributeValue.builder().s(profileId).build());
        item.put("email", AttributeValue.builder().s(request.getEmail()).build());
        item.put("username", AttributeValue.builder().s(request.getUsername()).build());
        item.put("first_name", AttributeValue.builder().s(request.getFirstName()).build());
        item.put("last_name", AttributeValue.builder().s(request.getLastName()).build());
        item.put("phone_number", AttributeValue.builder().s(request.getPhoneNumber()).build());
        item.put("created_at", AttributeValue.builder().s(now.toString()).build());
        item.put("updated_at", AttributeValue.builder().s(now.toString()).build());

        try {
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .conditionExpression("attribute_not_exists(email) AND attribute_not_exists(username)")
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            return CreateProfileResponse.builder()
                    .id(profileId)
                    .message("Profile created successfully")
                    .build();
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalStateException("Email or username already exists", e);
        }
    }

    public GetProfileResponse getProfile(String profileId) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName("Profiles")
                .key(Map.of("profileId", AttributeValue.builder().s(profileId).build()))
                .build();

            GetItemResponse response = dynamoDbClient.getItem(request);
            if (!response.hasItem()) {
                return GetProfileResponse.builder()
                    .success(false)
                    .message("Profile not found")
                    .build();
            }

            Profile profile = mapToProfile(response.item());
            return GetProfileResponse.builder()
                .success(true)
                .profile(profile)
                .build();
        } catch (Exception e) {
            log.error("Error getting profile: {}", e.getMessage());
            return GetProfileResponse.builder()
                .success(false)
                .message("Error getting profile")
                .build();
        }
    }

    public GetProfileResponse getProfileByEmail(String email) {
        try {
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("EmailIndex")
                .keyConditionExpression("email = :email")
                .expressionAttributeValues(Map.of(":email", AttributeValue.builder().s(email).build()))
                .build();

            QueryResponse response = dynamoDbClient.query(request);
            if (response.items().isEmpty()) {
                return GetProfileResponse.builder()
                    .success(false)
                    .message("Profile not found")
                    .build();
            }

            Profile profile = mapToProfile(response.items().get(0));
            return GetProfileResponse.builder()
                .success(true)
                .profile(profile)
                .build();
        } catch (Exception e) {
            log.error("Error getting profile by email: {}", e.getMessage());
            return GetProfileResponse.builder()
                .success(false)
                .message("Error getting profile")
                .build();
        }
    }

    public UpdateProfileResponse updateProfile(String profileId, UpdateProfileRequest request) {
        Instant now = Instant.now();
        Map<String, AttributeValue> key = Map.of("profile_id", AttributeValue.builder().s(profileId).build());
        
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        StringBuilder updateExpression = new StringBuilder("SET updated_at = :updated_at");

        expressionAttributeValues.put(":updated_at", AttributeValue.builder().s(now.toString()).build());

        if (request.getUsername() != null) {
            updateExpression.append(", username = :username");
            expressionAttributeNames.put("#username", "username");
            expressionAttributeValues.put(":username", AttributeValue.builder().s(request.getUsername()).build());
        }

        if (request.getFirstName() != null) {
            updateExpression.append(", first_name = :first_name");
            expressionAttributeNames.put("#first_name", "first_name");
            expressionAttributeValues.put(":first_name", AttributeValue.builder().s(request.getFirstName()).build());
        }

        if (request.getLastName() != null) {
            updateExpression.append(", last_name = :last_name");
            expressionAttributeNames.put("#last_name", "last_name");
            expressionAttributeValues.put(":last_name", AttributeValue.builder().s(request.getLastName()).build());
        }

        if (request.getPhoneNumber() != null) {
            updateExpression.append(", phone_number = :phone_number");
            expressionAttributeNames.put("#phone_number", "phone_number");
            expressionAttributeValues.put(":phone_number", AttributeValue.builder().s(request.getPhoneNumber()).build());
        }

        try {
            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .updateExpression(updateExpression.toString())
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .conditionExpression("attribute_exists(profile_id)")
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);
            return UpdateProfileResponse.builder()
                    .message("Profile updated successfully")
                    .build();
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalStateException("Profile not found: " + profileId, e);
        }
    }

    public void deleteProfile(String profileId) {
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("profile_id", AttributeValue.builder().s(profileId).build()))
                .conditionExpression("attribute_exists(profile_id)")
                .build();

        try {
            dynamoDbClient.deleteItem(deleteItemRequest);
        } catch (ConditionalCheckFailedException e) {
            throw new IllegalStateException("Profile not found: " + profileId, e);
        }
    }

    public ListProfilesResponse listProfiles() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        List<ProfileSummary> profiles = new ArrayList<>();
        String lastEvaluatedKey = null;

        do {
            ScanResponse result = dynamoDbClient.scan(scanRequest);
            profiles.addAll(result.items().stream()
                    .map(this::mapToProfileSummary)
                    .collect(Collectors.toList()));

            Map<String, AttributeValue> lastKey = result.lastEvaluatedKey();
            lastEvaluatedKey = (lastKey != null && lastKey.containsKey("profile_id")) 
                ? lastKey.get("profile_id").s() 
                : null;
            if (lastEvaluatedKey != null) {
                scanRequest = ScanRequest.builder()
                        .tableName(TABLE_NAME)
                        .exclusiveStartKey(result.lastEvaluatedKey())
                        .build();
            }
        } while (lastEvaluatedKey != null);

        return ListProfilesResponse.builder()
                .profiles(profiles)
                .build();
    }

    private ProfileSummary mapToProfileSummary(Map<String, AttributeValue> item) {
        return ProfileSummary.builder()
            .profileId(getStringValue(item, "profile_id"))
            .username(getStringValue(item, "username"))
            .firstName(getStringValue(item, "first_name"))
            .lastName(getStringValue(item, "last_name"))
            .email(getStringValue(item, "email"))
            .build();
    }

    private String getStringValue(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value != null ? value.s() : null;
    }

    private Profile mapToProfile(Map<String, AttributeValue> item) {
        return Profile.builder()
            .profileId(getStringValue(item, "profile_id"))
            .username(getStringValue(item, "username"))
            .firstName(getStringValue(item, "first_name"))
            .lastName(getStringValue(item, "last_name"))
            .email(getStringValue(item, "email"))
            .phoneNumber(getStringValue(item, "phone_number"))
            .build();
    }
}
