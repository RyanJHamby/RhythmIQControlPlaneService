package com.rhythmiq.controlplaneservice.api.airule;

import javax.inject.Inject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.rhythmiq.controlplaneservice.common.BaseLambdaHandler;
import com.rhythmiq.controlplaneservice.dao.AiRuleDao;

import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Log4j2
public class GetAiRulesLambdaHandler extends BaseLambdaHandler {

    private final AiRuleDao aiRuleDao;

    public GetAiRulesLambdaHandler() {
        this(DynamoDbClient.create());
    }

    @Inject
    public GetAiRulesLambdaHandler(DynamoDbClient dynamoDbClient) {
        this.aiRuleDao = new AiRuleDao(dynamoDbClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            return createSuccessResponse(200, aiRuleDao.getAllRules());
        } catch (Exception e) {
            log.error("Error retrieving AI rules", e);
            return createErrorResponse(500, "Failed to retrieve AI rules.");
        }
    }
} 