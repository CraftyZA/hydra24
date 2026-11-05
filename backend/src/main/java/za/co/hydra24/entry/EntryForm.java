package za.co.hydra24.entry;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** What someone chooses when entering an event. The price is worked out by the server. */
@RegisterForReflection
public record EntryForm(@NotNull Long classId, @NotBlank String attendance) {
}
