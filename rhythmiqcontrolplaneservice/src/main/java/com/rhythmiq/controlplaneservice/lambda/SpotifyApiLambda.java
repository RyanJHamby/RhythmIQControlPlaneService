package com.rhythmiq.controlplaneservice.lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhythmiq.controlplaneservice.spotify.SpotifyService;

public class SpotifyApiLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final SpotifyService spotifyService;
    private final ObjectMapper objectMapper;

    public SpotifyApiLambda() {
        this.spotifyService = new SpotifyService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        response.setHeaders(headers);

        try {
            String path = input.getPath();
            String httpMethod = input.getHttpMethod();
            Map<String, String> queryStringParameters = input.getQueryStringParameters();
            String sessionId = queryStringParameters != null ? queryStringParameters.get("sessionId") : null;
            String offset = queryStringParameters != null ? queryStringParameters.get("offset") : null;

            if (sessionId == null) {
                response.setStatusCode(400);
                response.setBody("{\"error\":\"Missing session ID\"}");
                return response;
            }

            String responseBody;
            if (path.equals("/spotify/liked-songs") && "GET".equals(httpMethod)) {
                responseBody = spotifyService.getLikedSongs(sessionId, offset != null ? Integer.parseInt(offset) : 0);
                response.setStatusCode(200);
                response.setBody(responseBody);
            } else if (path.equals("/spotify/playlists") && "GET".equals(httpMethod)) {
                responseBody = spotifyService.getPlaylists(sessionId);
                response.setStatusCode(200);
                response.setBody(responseBody);
            } else if (path.equals("/spotify/me") && "GET".equals(httpMethod)) {
                responseBody = spotifyService.getCurrentUser(sessionId);
                response.setStatusCode(200);
                response.setBody(responseBody);
            } else {
                response.setStatusCode(404);
                response.setBody("{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"" + e.getMessage() + "\"}");
        }

        return response;
    }
} 