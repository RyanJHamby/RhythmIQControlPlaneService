package com.rhythmiq.controlplaneservice.lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.rhythmiq.controlplaneservice.di.DaggerSpotifyComponent;
import com.rhythmiq.controlplaneservice.di.SpotifyComponent;
import com.rhythmiq.controlplaneservice.spotify.SpotifyService;

public class SpotifyApiLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final SpotifyService spotifyService;

    public SpotifyApiLambda() {
        SpotifyComponent component = DaggerSpotifyComponent.create();
        this.spotifyService = component.spotifyService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String path = input.getPath();
            String responseBody;

            if (path.startsWith("/spotify/liked-songs")) {
                String offset = input.getQueryStringParameters() != null ? 
                    input.getQueryStringParameters().getOrDefault("offset", "0") : "0";
                responseBody = spotifyService.getLikedSongs(Integer.parseInt(offset));
            } else if (path.startsWith("/spotify/playlists")) {
                responseBody = spotifyService.getPlaylists();
            } else {
                return createResponse(404, "{\"error\": \"Not found\"}");
            }

            return createResponse(200, responseBody);
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return createResponse(500, "{\"error\": \"Internal server error\"}");
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setHeaders(headers);
        response.setBody(body);
        return response;
    }
} 