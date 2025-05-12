package com.rhythmiq.controlplaneservice.spotify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

public class SpotifyService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SsmClient ssmClient;
    private String clientCredentialsToken;
    private static final Map<String, Map<String, String>> userSessions = new HashMap<>();
    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";

    public SpotifyService() {
        this.ssmClient = SsmClient.builder()
            .region(Region.US_EAST_1)
            .build();
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    private String getParameterFromSSM(String parameterName) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(false)
                .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            return response.parameter().value();
        } catch (SsmException e) {
            throw new RuntimeException("Failed to get parameter from SSM: " + parameterName, e);
        }
    }

    public String getLikedSongs(String sessionId, int offset) throws IOException, InterruptedException {
        String accessToken = getUserToken(sessionId, "access_token");
        if (accessToken == null) {
            throw new RuntimeException("No access token found. User needs to authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.spotify.com/v1/me/tracks?limit=20&offset=" + offset))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getPlaylists(String sessionId) throws IOException, InterruptedException {
        String accessToken = getUserToken(sessionId, "access_token");
        if (accessToken == null) {
            throw new RuntimeException("No access token found. User needs to authenticate first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.spotify.com/v1/me/playlists?limit=50"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String getClientCredentialsToken() throws IOException, InterruptedException {
        try {
            if (clientCredentialsToken != null) {
                return clientCredentialsToken;
            }

            String clientId = getParameterFromSSM("/rhythmiq/spotify/client_id");
            String clientSecret = getParameterFromSSM("/rhythmiq/spotify/client_secret");

            String credentials = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get client credentials token: " + response.body());
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body());
            clientCredentialsToken = jsonResponse.get("access_token").asText();
            return clientCredentialsToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get client credentials token: " + e.getMessage(), e);
        }
    }

    public String getAccessToken(String sessionId, String code) throws IOException, InterruptedException {
        try {
            String clientId = getParameterFromSSM("/rhythmiq/spotify/client_id");
            String clientSecret = getParameterFromSSM("/rhythmiq/spotify/client_secret");
            String redirectUri = getParameterFromSSM("/rhythmiq/spotify/redirect_uri");

            String credentials = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes());
            
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "authorization_code");
            formData.put("code", code);
            formData.put("redirect_uri", redirectUri);

            String formBody = formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get access token: " + response.body());
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body());
            String accessToken = jsonResponse.get("access_token").asText();
            String refreshToken = jsonResponse.get("refresh_token").asText();
            
            // Store tokens in session
            Map<String, String> sessionTokens = new HashMap<>();
            sessionTokens.put("access_token", accessToken);
            sessionTokens.put("refresh_token", refreshToken);
            userSessions.put(sessionId, sessionTokens);
            
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token: " + e.getMessage(), e);
        }
    }

    public String getCurrentUser(String sessionId) throws IOException, InterruptedException {
        try {
            String accessToken = getUserToken(sessionId, "access_token");
            if (accessToken == null) {
                throw new RuntimeException("No access token found. User needs to authenticate first.");
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get user profile: " + response.body());
            }
            
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user profile: " + e.getMessage(), e);
        }
    }

    private String getUserToken(String sessionId, String tokenType) {
        Map<String, String> sessionTokens = userSessions.get(sessionId);
        return sessionTokens != null ? sessionTokens.get(tokenType) : null;
    }

    public String getRecommendations(String profileId, Map<String, Object> parameters) {
        try {
            // Get user's access token from session
            String accessToken = getAccessToken(profileId, "access_token");
            if (accessToken == null) {
                throw new IllegalStateException("No access token found for profile: " + profileId);
            }

            // Build recommendation request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_API_BASE + "/recommendations"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            // Add parameters to request
            String queryString = parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
            
            if (!queryString.isEmpty()) {
                request = HttpRequest.newBuilder(request.uri())
                    .uri(URI.create(request.uri() + "?" + queryString))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            }

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Failed to get recommendations: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting recommendations", e);
        }
    }
} 