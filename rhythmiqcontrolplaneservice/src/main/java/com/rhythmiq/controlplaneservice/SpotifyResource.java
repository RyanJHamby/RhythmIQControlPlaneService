package com.rhythmiq.controlplaneservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Path("/spotify")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SpotifyResource {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyResource.class);
    private static final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String CLIENT_ID_PARAM = "/rhythmiq/spotify/client_id";
    private static final String CLIENT_SECRET_PARAM = "/rhythmiq/spotify/client_secret";
    private static final Gson gson = new Gson();

    @POST
    @Path("/token")
    public Response exchangeCodeForToken(String requestBody) {
        try {
            logger.debug("Received request body: {}", requestBody);
            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);
            String code = jsonRequest.get("code").getAsString();
            logger.debug("Extracted code: {}", code);
            
            String clientId = getParameter(CLIENT_ID_PARAM);
            String clientSecret = getParameter(CLIENT_SECRET_PARAM);
            String redirectUri = System.getenv("SPOTIFY_REDIRECT_URI");

            logger.debug("Client ID: {}", clientId != null ? "present" : "missing");
            logger.debug("Client Secret: {}", clientSecret != null ? "present" : "missing");
            logger.debug("Redirect URI: {}", redirectUri);

            if (clientId == null || clientSecret == null) {
                logger.error("Spotify credentials not configured");
                return Response.serverError().entity("{\"error\":\"Spotify credentials not configured\"}").build();
            }

            if (redirectUri == null) {
                logger.error("SPOTIFY_REDIRECT_URI not configured");
                return Response.serverError().entity("{\"error\":\"SPOTIFY_REDIRECT_URI not configured\"}").build();
            }

            String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "authorization_code");
            formData.put("code", code);
            formData.put("redirect_uri", redirectUri);

            String formBody = formData.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            logger.debug("Sending request to Spotify with form body: {}", formBody);

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPOTIFY_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + auth)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Received response from Spotify: {}", response.body());
            return Response.ok(response.body()).build();
        } catch (Exception e) {
            logger.error("Error exchanging code for token", e);
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    private String getParameter(String paramName) {
        try (SsmClient ssmClient = SsmClient.builder()
                .region(Region.US_EAST_1)
                .build()) {
            
            logger.debug("Fetching parameter from SSM: {}", paramName);
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(paramName)
                    .withDecryption(true)
                    .build();
            
            GetParameterResponse result = ssmClient.getParameter(request);
            return result.parameter().value();
        } catch (Exception e) {
            logger.error("Error fetching parameter from SSM: {}", paramName, e);
            return null;
        }
    }
} 