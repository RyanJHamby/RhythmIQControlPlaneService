package com.rhythmiq.controlplaneservice;

import com.rhthymiq.controlplaneservice.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/profiles")
@Tag(name = "Profile API", description = "Operations related to user profiles")
public class ProfileResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new profile",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Profile created successfully",
                            content = @Content(schema = @Schema(implementation = CreateProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ValidationException.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict error",
                            content = @Content(schema = @Schema(implementation = ConflictException.class)))
            })
    public Response createProfile(CreateProfileRequest request) {
        return Response.status(Response.Status.CREATED).entity(new CreateProfileResponse()).build();
    }

    @GET
    @Path("/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a profile by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = GetProfileResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Profile not found",
                            content = @Content(schema = @Schema(implementation = NotFoundException.class)))
            })
    public Response getProfile(@PathParam("profileId") String profileId) {
        return Response.ok(new GetProfileResponse()).build();
    }

    @PUT
    @Path("/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                            content = @Content(schema = @Schema(implementation = UpdateProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ValidationException.class))),
                    @ApiResponse(responseCode = "404", description = "Profile not found",
                            content = @Content(schema = @Schema(implementation = NotFoundException.class)))
            })
    public Response updateProfile(@PathParam("profileId") String profileId, UpdateProfileRequest request) {
        return Response.ok(new UpdateProfileResponse()).build();
    }

    @DELETE
    @Path("/{profileId}")
    @Operation(summary = "Delete a profile",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Profile not found",
                            content = @Content(schema = @Schema(implementation = NotFoundException.class)))
            })
    public Response deleteProfile(@PathParam("profileId") String profileId) {
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all profiles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ListProfilesResponse.class)))
            })
    public Response listProfiles() {
        return Response.ok(new ListProfilesResponse()).build();
    }
}