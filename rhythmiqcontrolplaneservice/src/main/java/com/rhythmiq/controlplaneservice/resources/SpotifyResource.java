package com.rhythmiq.controlplaneservice.resources;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Path("/spotify")
public class SpotifyResource {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyResource.class);

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context UriInfo uriInfo) {
        try {
            // Get the exact client ID from SSM
            String clientId = getParameterFromSSM("/rhythmiq/spotify/client_id");
            // The redirect URI should be where Spotify will redirect back to after auth
            String redirectUri = "http://localhost:3000/api/spotify/callback";
            String scopes = "user-read-private user-read-email user-library-read playlist-read-private";
            
            // Generate a secure random state
            String state = java.util.UUID.randomUUID().toString();

            // Log the raw values
            logger.info("\nRaw values:");
            logger.info("Client ID: {}", clientId);
            logger.info("Redirect URI: {}", redirectUri);
            logger.info("Scopes: {}", scopes);
            logger.info("State: {}", state);

            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
            String encodedScopes = URLEncoder.encode(scopes, StandardCharsets.UTF_8);

            logger.info("\nEncoded values:");
            logger.info("Encoded Redirect URI: {}", encodedRedirectUri);
            logger.info("Encoded Scopes: {}", encodedScopes);

            String authUrl = String.format(
                "https://accounts.spotify.com/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=%s&state=%s",
                clientId,
                encodedRedirectUri,
                encodedScopes,
                state
            );

            // Log the final URL
            logger.info("\nGenerated auth URL:");
            logger.info("{}", authUrl);

            return Response.ok()
                .entity(new LoginResponse(authUrl))
                .header("Access-Control-Allow-Origin", "http://localhost:3000")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Credentials", "true")
                .build();
        } catch (Exception e) {
            logger.error("\nError generating login URL:");
            logger.error("Error details:", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to generate login URL"))
                .header("Access-Control-Allow-Origin", "http://localhost:3000")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .header("Access-Control-Allow-Credentials", "true")
                .build();
        }
    }

    @OPTIONS
    @Path("/login")
    public Response handleOptions() {
        return Response.ok()
            .header("Access-Control-Allow-Origin", "http://localhost:3000")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type")
            .header("Access-Control-Allow-Credentials", "true")
            .build();
    }

    private String getParameterFromSSM(String parameterName) {
        try {
            // Create SSM client
            SsmClient ssmClient = SsmClient.builder()
                .region(Region.US_EAST_1) // Adjust region as needed
                .build();

            // Get parameter
            GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(true) // Always try to decrypt
                .build();

            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
            String value = parameterResponse.parameter().value();
            
            // Log the parameter details
            logger.info("\nSSM Parameter Details:");
            logger.info("Parameter Name: {}", parameterName);
            logger.info("Parameter Type: {}", parameterResponse.parameter().type());
            logger.info("Parameter Value: {}", value);
            logger.info("Value Length: {}", value.length());
            logger.info("Is Encrypted: {}", parameterResponse.parameter().type().equals("SecureString"));
                
            return value;
        } catch (Exception e) {
            logger.error("\nError getting parameter from SSM:");
            logger.error("Parameter Name: {}", parameterName);
            logger.error("Error details:", e);
            throw new RuntimeException("Failed to get parameter from SSM: " + parameterName, e);
        }
    }

    private static class LoginResponse {
        private final String loginUrl;

        public LoginResponse(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public String getLoginUrl() {
            return loginUrl;
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
} 