package za.co.hydra24.user;

import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import za.co.hydra24.security.Caller;

import java.util.UUID;

/** The signed-in user. The site calls PUT once after every sign-in. */
@Path("/api/me")
public class MeResource {

    @Inject
    UserRepository users;

    @PUT
    public void signedIn(@Context ContainerRequestContext request) {
        Caller caller = Caller.of(request);
        users.recordSignIn(UUID.fromString(caller.sub()), caller.email());
    }
}
