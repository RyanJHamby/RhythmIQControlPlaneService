package com.rhythmiq.controlplaneservice;

import com.rhythmiq.controlplaneservice.model.*;
import com.rhythmiq.controlplaneservice.exception.ValidationException;
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

@Path("/profiles")
public class ProfileResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProfile(CreateProfileRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ValidationException())
                    .build();
        }

        CreateProfileResponse response = CreateProfileResponse.builder()
                .message("Profile created successfully")
                .build();

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(@PathParam("profileId") String profileId) {
        return Response.ok(GetProfileResponse.builder().build()).build();
    }

    @PUT
    @Path("/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfile(@PathParam("profileId") String profileId, UpdateProfileRequest request) {
        return Response.ok(UpdateProfileResponse.builder()
                .message("Profile updated successfully")
                .build()).build();
    }

    @DELETE
    @Path("/{profileId}")
    public Response deleteProfile(@PathParam("profileId") String profileId) {
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listProfiles() {
        return Response.ok(ListProfilesResponse.builder().build()).build();
    }
}