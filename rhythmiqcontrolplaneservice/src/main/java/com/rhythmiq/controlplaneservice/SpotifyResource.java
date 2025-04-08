package com.rhythmiq.controlplaneservice;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhythmiq.controlplaneservice.spotify.SpotifyService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

@Path("/spotify")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SpotifyResource {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyResource.class);
    private static final Map<String, Long> usedCodes = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long CODE_EXPIRY_MS = 60000; // 1 minute

    private final SpotifyService spotifyService;
    private final SsmClient ssmClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    static {
        // Clean up expired codes every minute
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            usedCodes.entrySet().removeIf(entry -> now - entry.getValue() > CODE_EXPIRY_MS);
        }, 1, 1, TimeUnit.MINUTES);
    }

    public SpotifyResource() {
        this.ssmClient = SsmClient.builder().build();
        this.clientId = getParameterFromSSM("/rhythmiq/spotify/client_id");
        this.clientSecret = getParameterFromSSM("/rhythmiq/spotify/client_secret");
        this.redirectUri = getParameterFromSSM("/rhythmiq/spotify/redirect_uri");
        this.spotifyService = new SpotifyService();
    }

    private String getParameterFromSSM(String parameterName) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(false)
                .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();
            logger.debug("Successfully retrieved parameter: {}", parameterName);
            return value;
        } catch (SsmException e) {
            logger.error("Failed to get parameter from SSM: {}", parameterName, e);
            throw new RuntimeException("Failed to get parameter from SSM: " + parameterName, e);
        }
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response login() {
        try {
            logger.debug("Client ID from SSM: {}", clientId != null ? "present" : "null");
            logger.debug("Client Secret from SSM: {}", clientSecret != null ? "present" : "null");
            logger.debug("Redirect URI from SSM: {}", redirectUri != null ? "present" : "null");

            if (clientId == null || clientSecret == null || redirectUri == null) {
                logger.error("Missing required configuration");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Missing required configuration")
                    .build();
            }

            String state = "state123"; // In production, use a secure random string
            String scope = "user-read-private user-read-email user-library-read playlist-read-private";

            URI authorizationUri = UriBuilder.fromUri("https://accounts.spotify.com/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("state", state)
                .build();

            return Response.seeOther(authorizationUri).build();
        } catch (Exception e) {
            logger.error("Error during login", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error during login: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state) {
        try {
            if (code == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing authorization code")
                    .build();
            }

            // Generate a session ID
            String sessionId = java.util.UUID.randomUUID().toString();
            
            // Exchange code for access token
            String accessToken = spotifyService.getAccessToken(sessionId, code);
            
            // Redirect to frontend with token and session ID
            URI frontendUri = UriBuilder.fromUri("http://localhost:3000")
                .queryParam("token", accessToken)
                .queryParam("sessionId", sessionId)
                .build();

            return Response.seeOther(frontendUri).build();
        } catch (Exception e) {
            logger.error("Error during callback", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error during callback: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/liked-songs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLikedSongs(@QueryParam("sessionId") String sessionId, @QueryParam("offset") Integer offset) {
        try {
            if (sessionId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing session ID\"}")
                    .build();
            }

            String response = spotifyService.getLikedSongs(sessionId, offset != null ? offset : 0);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error fetching liked songs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching liked songs: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/playlists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlaylists(@QueryParam("sessionId") String sessionId) {
        try {
            if (sessionId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing session ID\"}")
                    .build();
            }

            String response = spotifyService.getPlaylists(sessionId);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error fetching playlists", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching playlists: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUser(@QueryParam("sessionId") String sessionId) {
        try {
            if (sessionId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing session ID\"}")
                    .build();
            }

            String response = spotifyService.getCurrentUser(sessionId);
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.error("Error fetching user profile", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching user profile: " + e.getMessage())
                .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        Response.ResponseBuilder responseBuilder = Response.ok()
            .entity("{\"success\":true}")
            .cookie(new NewCookie.Builder("spotify_access_token")
                .value("")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(0)
                .build())
            .cookie(new NewCookie.Builder("spotify_refresh_token")
                .value("")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(0)
                .build());
        
        return responseBuilder.build();
    }
} 