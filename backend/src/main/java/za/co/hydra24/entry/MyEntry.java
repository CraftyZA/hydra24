package za.co.hydra24.entry;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One line in a member's list of their entries. */
@RegisterForReflection
public record MyEntry(long eventId, String eventName, LocalDate startsOn, String className,
                      String attendance, BigDecimal cost) {
}
