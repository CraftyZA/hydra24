package za.co.hydra24.entry;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One line in the caller's list of entries. */
@RegisterForReflection
public record MyEntry(long eventId, String eventName, LocalDate startsOn, String venueName,
                      String className, String attendance, BigDecimal cost) {
}
