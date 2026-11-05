package za.co.hydra24.entry;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.security.AdminOnly;

import java.util.List;

/** Who has entered an event. Leaves out the sensitive profile fields on purpose. */
@Path("/api/admin/events/{eventId}/entries")
@AdminOnly
@Produces(MediaType.APPLICATION_JSON)
public class EntrantAdminResource {

    @Inject
    EntryRepository entries;

    @GET
    public List<Entrant> list(@PathParam("eventId") long eventId) {
        return entries.listForEvent(eventId);
    }
}
