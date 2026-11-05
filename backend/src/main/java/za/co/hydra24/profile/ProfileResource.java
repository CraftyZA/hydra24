package za.co.hydra24.profile;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.security.Caller;

/** The caller's own profile, and only theirs. GET answers 204 until one is saved. */
@Path("/api/me/profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource {

    @Inject
    ProfileRepository profiles;

    @GET
    public Profile get(@Context ContainerRequestContext request) {
        return profiles.find(Caller.of(request).subject()).orElse(null);
    }

    @PUT
    public void save(@Context ContainerRequestContext request, @Valid Profile profile) {
        profiles.save(Caller.of(request).subject(), profile);
    }
}
