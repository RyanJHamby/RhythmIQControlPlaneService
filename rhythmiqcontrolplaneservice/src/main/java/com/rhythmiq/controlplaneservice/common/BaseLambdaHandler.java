package com.rhythmiq.controlplaneservice.common;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public abstract class BaseLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final Logger log = LogManager.getLogger(BaseLambdaHandler.class);

    @Override
    public abstract APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context);

    protected APIGatewayProxyResponseEvent createSuccessResponse(int statusCode, Object responseBody) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getHeaders())
                    .withBody(objectMapper.writeValueAsString(responseBody));
        } catch (Exception e) {
            log.error("Failed to serialize response", e);
            return createErrorResponse(500, "Internal Server Error");
        }
    }

    protected APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getHeaders())
                    .withBody(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
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
