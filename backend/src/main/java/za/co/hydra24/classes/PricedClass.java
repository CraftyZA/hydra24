package za.co.hydra24.classes;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;

/** A class open for entry, at the price that applies where it is being offered. */
@RegisterForReflection
public record PricedClass(long id, String name, String description, ClassType classType, BigDecimal cost) {
}
