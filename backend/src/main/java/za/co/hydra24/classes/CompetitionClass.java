package za.co.hydra24.classes;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.hydra24.event.EventType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * A class as admins manage it. {@code cost} is the flat entry price, and
 * {@code eventTypeCosts} holds only the event types that charge something else.
 * A disabled class stays on existing entries but cannot be chosen for new ones.
 */
@RegisterForReflection
public record CompetitionClass(
        Long id,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String description,
        @NotNull @Min(0) Integer sortOrder,
        @NotNull ClassType classType,
        boolean enabled,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal cost,
        @NotNull Map<EventType, @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal> eventTypeCosts) {
}
