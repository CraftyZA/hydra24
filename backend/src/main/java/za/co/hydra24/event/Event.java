package za.co.hydra24.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import za.co.hydra24.options.Option;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * An event on the calendar. Sent and received as is. An id or imagePath in a request
 * is ignored: the image is set through {@link EventImageAdminResource}.
 */
@RegisterForReflection
public record Event(
        Long id,
        @NotBlank @Size(max = 100) String name,
        @NotNull EventType eventType,
        @NotNull LocalDate startsOn,
        @NotNull @Min(1) @Max(30) Integer days,
        LocalDateTime entryClose,
        @Size(max = 10000) String itinerary,
        @Size(max = 10000) String accommodation,
        @Size(max = 10000) String catering,
        String imagePath) {

    private static final ZoneId EVENT_ZONE = ZoneId.of("Africa/Johannesburg");

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean entriesOpen() {
        return entriesOpenAt(LocalDateTime.now(EVENT_ZONE));
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<Option> attendanceOptions() {
        return Attendance.optionsFor(days);
    }

    public boolean offersAttendance(String attendance) {
        return attendanceOptions().stream().anyMatch(option -> option.value().equals(attendance));
    }

    /** Until the entry deadline. With no deadline, until the morning the event starts. */
    boolean entriesOpenAt(LocalDateTime now) {
        LocalDateTime deadline = entryClose != null ? entryClose : startsOn.atStartOfDay();
        return now.isBefore(deadline);
    }
}
