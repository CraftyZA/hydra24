package za.co.hydra24.entry;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;

/** The caller's entry at one event. */
@RegisterForReflection
public record Entry(long classId, String className, String attendance, String attendanceLabel, BigDecimal cost) {
}
