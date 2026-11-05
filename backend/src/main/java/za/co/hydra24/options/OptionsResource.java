package za.co.hydra24.options;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.classes.ClassType;
import za.co.hydra24.event.EventType;
import za.co.hydra24.profile.Disability;
import za.co.hydra24.profile.Gender;
import za.co.hydra24.profile.Race;

import java.util.List;

/** The enums, so the site's drop-downs have one source of truth: these. */
@Path("/api/options")
@Produces(MediaType.APPLICATION_JSON)
public class OptionsResource {

    private static final Options OPTIONS = new Options(
            Option.of(Gender.values()),
            Option.of(Race.values()),
            Option.of(Disability.values()),
            Option.of(ClassType.values()),
            Option.of(EventType.values()));

    @GET
    public Options list() {
        return OPTIONS;
    }

    @RegisterForReflection
    public record Options(List<Option> genders,
                          List<Option> races,
                          List<Option> disabilities,
                          List<Option> classTypes,
                          List<Option> eventTypes) {
    }
}
