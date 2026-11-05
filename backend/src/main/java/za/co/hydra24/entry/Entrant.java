package za.co.hydra24.entry;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;

/** One person entered in an event, as the organisers see them. */
@RegisterForReflection
public record Entrant(long profileId, String firstName, String lastName, String cellphone,
                      String className, String attendance, BigDecimal cost) {
}
