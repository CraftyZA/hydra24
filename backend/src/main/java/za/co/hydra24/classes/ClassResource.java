package za.co.hydra24.classes;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/** Public: the classes open for entry, at their own price. */
@Path("/api/classes")
@Produces(MediaType.APPLICATION_JSON)
public class ClassResource {

    @Inject
    ClassRepository classes;

    @GET
    public List<PricedClass> listEnabled() {
        return classes.listEnabled();
    }
}
