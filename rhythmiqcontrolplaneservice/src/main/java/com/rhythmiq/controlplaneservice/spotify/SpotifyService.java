package com.rhythmiq.controlplaneservice.spotify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

public class SpotifyService {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SsmClient ssmClient;

    public SpotifyService() {
        this.ssmClient = SsmClient.builder().build();
        this.clientId = getParameterFromSSM("/rhythmiq/spotify/client_id");
        this.clientSecret = getParameterFromSSM("/rhythmiq/spotify/client_secret");
        this.redirectUri = getParameterFromSSM("/rhythmiq/spotify/redirect_uri");
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

    public String getLikedSongs(int offset) throws Exception {
        String accessToken = getAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.spotify.com/v1/me/tracks?limit=20&offset=" + offset))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getPlaylists() throws Exception {
        String accessToken = getAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.spotify.com/v1/me/playlists?limit=50"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String getAccessToken() throws Exception {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://accounts.spotify.com/api/token"))
            .header("Authorization", "Basic " + encodedAuth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonResponse = objectMapper.readTree(response.body());
        return jsonResponse.get("access_token").asText();
    }
} 