package za.co.hydra24.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.hydra24.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EventRepository {

    private static final String COLUMNS = """
            id, name, event_type, starts_on, days, entry_close, venue_name, venue_address,
            itinerary, accommodation, catering
            """;

    private static final String LIST = "SELECT " + COLUMNS + " FROM events ORDER BY starts_on, name";

    private static final String FIND = "SELECT " + COLUMNS + " FROM events WHERE id = ?";

    private static final String INSERT = """
            INSERT INTO events (name, event_type, starts_on, days, entry_close, venue_name, venue_address,
                                itinerary, accommodation, catering)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String UPDATE = """
            UPDATE events
               SET name = ?, event_type = ?, starts_on = ?, days = ?, entry_close = ?, venue_name = ?,
                   venue_address = ?, itinerary = ?, accommodation = ?, catering = ?
             WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM events WHERE id = ?";

    @Inject
    Database database;

    public List<Event> list() {
        return database.query(LIST, EventRepository::toEvent);
    }

    public Optional<Event> find(long id) {
        return database.queryOne(FIND, EventRepository::toEvent, id);
    }

    public long create(Event e) {
        return database.queryOne(INSERT, row -> row.getLong("id"),
                e.name(), e.eventType().name(), e.startsOn(), e.days(), e.entryClose(), e.venueName(),
                e.venueAddress(), e.itinerary(), e.accommodation(), e.catering()).orElseThrow();
    }

    /** False when there is no such event. */
    public boolean update(long id, Event e) {
        return database.update(UPDATE,
                e.name(), e.eventType().name(), e.startsOn(), e.days(), e.entryClose(), e.venueName(),
                e.venueAddress(), e.itinerary(), e.accommodation(), e.catering(), id) > 0;
    }

    /** False when there is no such event. */
    public boolean delete(long id) {
        return database.update(DELETE, id) > 0;
    }

    private static Event toEvent(ResultSet row) throws SQLException {
        return new Event(
                row.getLong("id"),
                row.getString("name"),
                EventType.valueOf(row.getString("event_type")),
                row.getObject("starts_on", LocalDate.class),
                row.getInt("days"),
                row.getObject("entry_close", LocalDateTime.class),
                row.getString("venue_name"),
                row.getString("venue_address"),
                row.getString("itinerary"),
                row.getString("accommodation"),
                row.getString("catering"));
    }
}
