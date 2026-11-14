package za.co.hydra24.event;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.api.ApiErrors;
import za.co.hydra24.db.ForeignKeyViolationException;
import za.co.hydra24.security.AdminOnly;

@Path("/api/admin/events")
@AdminOnly
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventAdminResource {

    @Inject
    EventRepository events;

    @Inject
    EventImages images;

    @POST
    public Event create(@Valid Event event) {
        return events.find(events.create(event)).orElseThrow();
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") long id, @Valid Event event) {
        if (!events.update(id, event)) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        String imageKey = events.findImageKey(id).orElse(null);
        try {
            if (!events.delete(id)) {
                throw new NotFoundException();
            }
        } catch (ForeignKeyViolationException e) {
            throw ApiErrors.conflict("This event has entries, so it cannot be deleted.");
        }
        if (imageKey != null) {
            images.delete(imageKey);
        }
    }
}
