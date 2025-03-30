package com.rhythmiq.controlplaneservice.api.airule;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.AiRuleDao;
import com.rhythmiq.controlplaneservice.model.AiRule;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Inject;

@Log4j2
public class CreateAiRuleLambdaHandler extends BaseLambdaHandler {

    private final AiRuleDao aiRuleDao;

    public CreateAiRuleLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public CreateAiRuleLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.aiRuleDao = new AiRuleDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        AiRule rule;
        try {
            rule = objectMapper.readValue(request.getBody(), AiRule.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body", e);
            return createErrorResponse(400, "Invalid request format.");
        }

        try {
            AiRule createdRule = aiRuleDao.createRule(rule);
            return createSuccessResponse(201, createdRule);
        } catch (Exception e) {
            log.error("Error creating AI rule", e);
            return createErrorResponse(500, "Failed to create AI rule.");
        }
    }
} 