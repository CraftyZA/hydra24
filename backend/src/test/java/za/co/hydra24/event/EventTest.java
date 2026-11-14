package za.co.hydra24.event;

import org.junit.jupiter.api.Test;
import za.co.hydra24.options.Option;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventTest {

    private static final LocalDate STARTS_ON = LocalDate.of(2027, 3, 10);

    @Test
    void entriesCloseAtTheDeadline() {
        Event event = event(1, STARTS_ON.minusDays(7).atTime(18, 0));
        assertTrue(event.entriesOpenAt(event.entryClose().minusMinutes(1)));
        assertFalse(event.entriesOpenAt(event.entryClose()));
    }

    @Test
    void withNoDeadlineEntriesCloseWhenTheEventStarts() {
        Event event = event(1, null);
        assertTrue(event.entriesOpenAt(STARTS_ON.minusDays(1).atTime(23, 59)));
        assertFalse(event.entriesOpenAt(STARTS_ON.atStartOfDay()));
    }

    @Test
    void aOneDayEventOffersOnlyThatDay() {
        assertEquals(List.of(new Option("DAY_1", "Day 1")), event(1, null).attendanceOptions());
    }

    @Test
    void aLongerEventOffersAllDaysAndEachDay() {
        assertEquals(List.of(
                        new Option("ALL_DAYS", "All days"),
                        new Option("DAY_1", "Day 1"),
                        new Option("DAY_2", "Day 2")),
                event(2, null).attendanceOptions());
    }

    @Test
    void attendanceMustBeOneOfTheOptions() {
        Event twoDays = event(2, null);
        assertTrue(twoDays.offersAttendance("DAY_2"));
        assertFalse(twoDays.offersAttendance("DAY_3"));
        assertFalse(event(1, null).offersAttendance("ALL_DAYS"));
    }

    private static Event event(int days, LocalDateTime entryClose) {
        return new Event(1L, "Test", EventType.HYDRA_24, STARTS_ON, days, entryClose,
                null, null, null, null);
    }
}
