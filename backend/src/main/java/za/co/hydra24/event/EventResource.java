package za.co.hydra24.event;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.classes.ClassRepository;
import za.co.hydra24.classes.PricedClass;

import java.util.List;

/** Public: the calendar and what each event offers. */
@Path("/api/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    EventRepository events;

    @Inject
    ClassRepository classes;

    @GET
    public List<Event> list() {
        return events.list();
    }

    @GET
    @Path("/{id}")
    public Event get(@PathParam("id") long id) {
        return events.find(id).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{id}/classes")
    public List<PricedClass> classes(@PathParam("id") long id) {
        return classes.listEnabledFor(get(id).eventType());
    }
}
