package za.co.hydra24.entry;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.security.Caller;

/** The caller's own entry at an event. GET answers 204 when they have none. */
@Path("/api/events/{eventId}/entry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntryResource {

    @Inject
    EntryService entries;

    @GET
    public Entry get(@Context ContainerRequestContext request, @PathParam("eventId") long eventId) {
        return entries.find(Caller.of(request).subject(), eventId).orElse(null);
    }

    @PUT
    public void enter(@Context ContainerRequestContext request, @PathParam("eventId") long eventId,
                      @Valid EntryForm form) {
        entries.enter(Caller.of(request).subject(), eventId, form);
    }

    @DELETE
    public void withdraw(@Context ContainerRequestContext request, @PathParam("eventId") long eventId) {
        entries.withdraw(Caller.of(request).subject(), eventId);
    }
}
