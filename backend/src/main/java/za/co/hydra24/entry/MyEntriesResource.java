package za.co.hydra24.entry;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.security.Caller;

import java.util.List;

@Path("/api/me/entries")
@Produces(MediaType.APPLICATION_JSON)
public class MyEntriesResource {

    @Inject
    EntryService entries;

    @GET
    public List<MyEntry> list(@Context ContainerRequestContext request) {
        return entries.listMine(Caller.of(request).subject());
    }
}
