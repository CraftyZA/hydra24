package za.co.hydra24.classes;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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

import java.util.List;

@Path("/api/admin/classes")
@AdminOnly
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClassAdminResource {

    @Inject
    ClassRepository classes;

    @GET
    public List<CompetitionClass> list() {
        return classes.listAll();
    }

    @GET
    @Path("/{id}")
    public CompetitionClass get(@PathParam("id") long id) {
        return classes.find(id).orElseThrow(NotFoundException::new);
    }

    @POST
    public CompetitionClass create(@Valid CompetitionClass competitionClass) {
        return get(classes.create(competitionClass));
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") long id, @Valid CompetitionClass competitionClass) {
        if (!classes.update(id, competitionClass)) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        try {
            if (!classes.delete(id)) {
                throw new NotFoundException();
            }
        } catch (ForeignKeyViolationException e) {
            throw ApiErrors.conflict("This class has entries. Disable it instead.");
        }
    }
}
