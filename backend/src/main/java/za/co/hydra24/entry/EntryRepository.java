package za.co.hydra24.entry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.hydra24.db.Database;
import za.co.hydra24.event.Attendance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EntryRepository {

    private static final String FIND = """
            SELECT e.class_id, c.name AS class_name, e.attendance, e.cost
              FROM entries e
              JOIN classes c ON c.id = e.class_id
             WHERE e.event_id = ? AND e.profile_id = ?
            """;

    private static final String SAVE = """
            INSERT INTO entries (event_id, profile_id, class_id, attendance, cost)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (event_id, profile_id) DO UPDATE
               SET class_id = EXCLUDED.class_id,
                   attendance = EXCLUDED.attendance,
                   cost = EXCLUDED.cost
            """;

    private static final String DELETE = "DELETE FROM entries WHERE event_id = ? AND profile_id = ?";

    private static final String LIST_FOR_PROFILE = """
            SELECT e.event_id, ev.name AS event_name, ev.starts_on,
                   c.name AS class_name, e.attendance, e.cost
              FROM entries e
              JOIN events ev ON ev.id = e.event_id
              JOIN classes c ON c.id = e.class_id
             WHERE e.profile_id = ?
             ORDER BY ev.starts_on DESC
            """;

    private static final String LIST_FOR_EVENT = """
            SELECT e.profile_id, p.first_name, p.last_name, p.cellphone,
                   c.name AS class_name, e.attendance, e.cost
              FROM entries e
              JOIN profiles p ON p.id = e.profile_id
              JOIN classes c ON c.id = e.class_id
             WHERE e.event_id = ?
             ORDER BY c.sort_order, p.last_name, p.first_name
            """;

    @Inject
    Database database;

    public Optional<Entry> find(long eventId, long profileId) {
        return database.queryOne(FIND, row -> new Entry(
                        row.getLong("class_id"),
                        row.getString("class_name"),
                        row.getString("attendance"),
                        Attendance.label(row.getString("attendance")),
                        row.getBigDecimal("cost")),
                eventId, profileId);
    }

    /** Enters the person, or changes their existing entry at this event. */
    public void save(long eventId, long profileId, long classId, String attendance, BigDecimal cost) {
        database.update(SAVE, eventId, profileId, classId, attendance, cost);
    }

    public void delete(long eventId, long profileId) {
        database.update(DELETE, eventId, profileId);
    }

    public List<MyEntry> listForProfile(long profileId) {
        return database.query(LIST_FOR_PROFILE, row -> new MyEntry(
                        row.getLong("event_id"),
                        row.getString("event_name"),
                        row.getObject("starts_on", LocalDate.class),
                        row.getString("class_name"),
                        Attendance.label(row.getString("attendance")),
                        row.getBigDecimal("cost")),
                profileId);
    }

    public List<Entrant> listForEvent(long eventId) {
        return database.query(LIST_FOR_EVENT, row -> new Entrant(
                        row.getLong("profile_id"),
                        row.getString("first_name"),
                        row.getString("last_name"),
                        row.getString("cellphone"),
                        row.getString("class_name"),
                        Attendance.label(row.getString("attendance")),
                        row.getBigDecimal("cost")),
                eventId);
    }
}
