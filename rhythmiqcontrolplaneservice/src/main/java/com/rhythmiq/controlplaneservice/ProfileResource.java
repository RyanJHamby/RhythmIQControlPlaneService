package com.rhythmiq.controlplaneservice;

import com.rhythmiq.controlplaneservice.model.*;
import com.rhythmiq.controlplaneservice.exception.ValidationException;
import com.rhythmiq.controlplaneservice.dao.ProfileDao;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/profiles")
public class ProfileResource {
    private final ProfileDao profileDao;

    public ProfileResource(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProfile(CreateProfileRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() || request.getUsername().length() < 3) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ValidationException("Username must be at least 3 characters long", Map.of("username", 1)))
                    .build();
        }

        CreateProfileResponse response = profileDao.createProfile(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@PathParam("profileId") String profileId) {
        GetProfileResponse response = profileDao.getProfile(profileId);
        if (!response.isSuccess()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(response)
                    .build();
        }
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@PathParam("profileId") String profileId, UpdateProfileRequest request) {
        if (request.getUsername() != null && (request.getUsername().isEmpty() || request.getUsername().length() < 3)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ValidationException("Username must be at least 3 characters long", Map.of("username", 1)))
                    .build();
        }

        try {
            UpdateProfileResponse response = profileDao.updateProfile(profileId, request);
            return Response.ok(response).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(GetProfileResponse.builder()
                            .success(false)
                            .message("Profile not found")
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{profileId}")
    public Response deleteProfile(@PathParam("profileId") String profileId) {
        try {
            profileDao.deleteProfile(profileId);
            return Response.noContent().build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(GetProfileResponse.builder()
                            .success(false)
                            .message("Profile not found")
                            .build())
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listProfiles() {
        return Response.ok(profileDao.listProfiles()).build();
    }
}