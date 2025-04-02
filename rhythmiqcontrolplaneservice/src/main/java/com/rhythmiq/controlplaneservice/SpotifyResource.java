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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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

            logger.debug("Client ID from SSM: {}", clientId != null ? "present" : "null");
            logger.debug("Client Secret from SSM: {}", clientSecret != null ? "present" : "null");
            logger.debug("Redirect URI from env: {}", redirectUri);

            if (clientId == null || clientSecret == null) {
                logger.error("Spotify credentials not configured - Client ID: {}, Client Secret: {}", 
                    clientId == null ? "missing" : "present",
                    clientSecret == null ? "missing" : "present");
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

            logger.debug("Full request details:");
            logger.debug("URL: {}", SPOTIFY_TOKEN_URL);
            logger.debug("Form body: {}", formBody);
            logger.debug("Authorization header: Basic {}", auth);

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SPOTIFY_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + auth)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Received response from Spotify: {}", response.body());
            logger.debug("Response status code: {}", response.statusCode());
            
            if (response.statusCode() != 200) {
                logger.error("Spotify API error: {} - {}", response.statusCode(), response.body());
                return Response.serverError().entity(response.body()).build();
            }
            
            return Response.ok(response.body()).build();
        } catch (Exception e) {
            logger.error("Error exchanging code for token: {}", e.getMessage(), e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Failed to exchange code for token");
            errorResponse.addProperty("details", e.getMessage());
            return Response.serverError().entity(errorResponse.toString()).build();
        }
    }

    @GET
    @Path("/test-credentials")
    public Response testCredentials() {
        String clientId = getParameter(CLIENT_ID_PARAM);
        String clientSecret = getParameter(CLIENT_SECRET_PARAM);
        String redirectUri = System.getenv("SPOTIFY_REDIRECT_URI");
        
        JsonObject response = new JsonObject();
        response.addProperty("clientIdPresent", clientId != null);
        response.addProperty("clientSecretPresent", clientSecret != null);
        response.addProperty("redirectUriPresent", redirectUri != null);
        
        return Response.ok(response.toString()).build();
    }

    private String getParameter(String paramName) {
        try {
            logger.debug("Attempting to fetch parameter from SSM: {}", paramName);
            SsmClient ssmClient = SsmClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            
            logger.debug("Created SSM client with region: {}", Region.US_EAST_1);
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(paramName)
                    .withDecryption(true)
                    .build();
            
            logger.debug("Built GetParameterRequest for: {}", paramName);
            GetParameterResponse result = ssmClient.getParameter(request);
            logger.debug("Successfully retrieved parameter: {}", paramName);
            return result.parameter().value();
        } catch (Exception e) {
            logger.error("Error fetching parameter from SSM: {} - Error: {}", paramName, e.getMessage(), e);
            return null;
        }
    }
} 