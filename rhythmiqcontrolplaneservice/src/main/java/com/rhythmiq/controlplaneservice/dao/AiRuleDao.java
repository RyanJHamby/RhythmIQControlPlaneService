package com.rhythmiq.controlplaneservice.dao;

import com.rhythmiq.controlplaneservice.model.AiRule;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class AiRuleDao {
    private static final String TABLE_NAME = "AiRules";
    private final DynamoDbClient dynamoDbClient;

    public AiRuleDao(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public AiRule createRule(AiRule rule) {
        String ruleId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(Map.of(
                        "ruleId", AttributeValue.builder().s(ruleId).build(),
                        "name", AttributeValue.builder().s(rule.getName()).build(),
                        "description", AttributeValue.builder().s(rule.getDescription()).build(),
                        "category", AttributeValue.builder().s(rule.getCategory()).build(),
                        "content", AttributeValue.builder().s(rule.getContent()).build(),
                        "isActive", AttributeValue.builder().bool(rule.isActive()).build(),
                        "createdAt", AttributeValue.builder().s(timestamp).build(),
                        "updatedAt", AttributeValue.builder().s(timestamp).build()
                ))
                .build();

        dynamoDbClient.putItem(putItemRequest);
        rule.setRuleId(ruleId);
        rule.setCreatedAt(timestamp);
        rule.setUpdatedAt(timestamp);
        return rule;
    }

    public List<AiRule> getAllRules() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        List<AiRule> rules = new ArrayList<>();

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            rules.add(mapToAiRule(item));
        }

        return rules;
    }

    public AiRule getRule(String ruleId) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("ruleId", AttributeValue.builder().s(ruleId).build()))
                .build();

        GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);
        if (getItemResponse.hasItem()) {
            return mapToAiRule(getItemResponse.item());
        }
        return null;
    }

    public AiRule updateRule(AiRule rule) {
        String timestamp = Instant.now().toString();
        
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("ruleId", AttributeValue.builder().s(rule.getRuleId()).build()))
                .updateExpression("SET #name = :name, #description = :description, #category = :category, " +
                        "#content = :content, #isActive = :isActive, #updatedAt = :updatedAt")
                .expressionAttributeNames(Map.of(
                        "#name", "name",
                        "#description", "description",
                        "#category", "category",
                        "#content", "content",
                        "#isActive", "isActive",
                        "#updatedAt", "updatedAt"
                ))
                .expressionAttributeValues(Map.of(
                        ":name", AttributeValue.builder().s(rule.getName()).build(),
                        ":description", AttributeValue.builder().s(rule.getDescription()).build(),
                        ":category", AttributeValue.builder().s(rule.getCategory()).build(),
                        ":content", AttributeValue.builder().s(rule.getContent()).build(),
                        ":isActive", AttributeValue.builder().bool(rule.isActive()).build(),
                        ":updatedAt", AttributeValue.builder().s(timestamp).build()
                ))
                .build();

        dynamoDbClient.updateItem(updateItemRequest);
        rule.setUpdatedAt(timestamp);
        return rule;
    }

    public void deleteRule(String ruleId) {
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("ruleId", AttributeValue.builder().s(ruleId).build()))
                .build();

        dynamoDbClient.deleteItem(deleteItemRequest);
    }

    private AiRule mapToAiRule(Map<String, AttributeValue> item) {
        return AiRule.builder()
                .ruleId(item.get("ruleId").s())
                .name(item.get("name").s())
                .description(item.get("description").s())
                .category(item.get("category").s())
                .content(item.get("content").s())
                .isActive(item.get("isActive").bool())
                .createdAt(item.get("createdAt").s())
                .updatedAt(item.get("updatedAt").s())
                .build();
    }
} 