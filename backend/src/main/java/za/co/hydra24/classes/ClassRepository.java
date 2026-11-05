package za.co.hydra24.classes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import za.co.hydra24.db.Database;
import za.co.hydra24.event.EventType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ClassRepository {

    private static final String LIST_ALL = """
            SELECT id, name, description, sort_order, class_type, enabled, cost
              FROM classes
             ORDER BY sort_order, name
            """;

    private static final String LIST_EVENT_TYPE_COSTS = "SELECT class_id, event_type, cost FROM class_event_type_costs";

    private static final String LIST_ENABLED = """
            SELECT id, name, description, class_type, cost
              FROM classes
             WHERE enabled
             ORDER BY sort_order, name
            """;

    // The one place an event type's price override meets the class's own price.
    private static final String PRICED_FOR_EVENT_TYPE = """
            SELECT c.id, c.name, c.description, c.class_type, COALESCE(o.cost, c.cost) AS cost
              FROM classes c
              LEFT JOIN class_event_type_costs o ON o.class_id = c.id AND o.event_type = ?
             WHERE c.enabled
             ORDER BY c.sort_order, c.name
            """;

    private static final String INSERT = """
            INSERT INTO classes (name, description, sort_order, class_type, enabled, cost)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String UPDATE = """
            UPDATE classes
               SET name = ?, description = ?, sort_order = ?, class_type = ?, enabled = ?, cost = ?
             WHERE id = ?
            """;

    private static final String DELETE = "DELETE FROM classes WHERE id = ?";

    private static final String DELETE_EVENT_TYPE_COSTS = "DELETE FROM class_event_type_costs WHERE class_id = ?";

    private static final String INSERT_EVENT_TYPE_COST =
            "INSERT INTO class_event_type_costs (class_id, event_type, cost) VALUES (?, ?, ?)";

    @Inject
    Database database;

    public List<CompetitionClass> listAll() {
        Map<Long, Map<EventType, BigDecimal>> eventTypeCosts = eventTypeCostsByClass();
        return database.query(LIST_ALL, row -> {
            long id = row.getLong("id");
            return new CompetitionClass(
                    id,
                    row.getString("name"),
                    row.getString("description"),
                    row.getInt("sort_order"),
                    ClassType.valueOf(row.getString("class_type")),
                    row.getBoolean("enabled"),
                    row.getBigDecimal("cost"),
                    eventTypeCosts.getOrDefault(id, Map.of()));
        });
    }

    public Optional<CompetitionClass> find(long id) {
        return listAll().stream().filter(c -> c.id() == id).findFirst();
    }

    public List<PricedClass> listEnabled() {
        return database.query(LIST_ENABLED, ClassRepository::toPricedClass);
    }

    public List<PricedClass> listEnabledFor(EventType eventType) {
        return database.query(PRICED_FOR_EVENT_TYPE, ClassRepository::toPricedClass, eventType.name());
    }

    @Transactional
    public long create(CompetitionClass c) {
        long id = database.queryOne(INSERT, row -> row.getLong("id"),
                c.name(), c.description(), c.sortOrder(), c.classType().name(), c.enabled(), c.cost()).orElseThrow();
        insertEventTypeCosts(id, c.eventTypeCosts());
        return id;
    }

    /** False when there is no such class. */
    @Transactional
    public boolean update(long id, CompetitionClass c) {
        int changed = database.update(UPDATE,
                c.name(), c.description(), c.sortOrder(), c.classType().name(), c.enabled(), c.cost(), id);
        if (changed == 0) {
            return false;
        }
        database.update(DELETE_EVENT_TYPE_COSTS, id);
        insertEventTypeCosts(id, c.eventTypeCosts());
        return true;
    }

    /** False when there is no such class. */
    public boolean delete(long id) {
        return database.update(DELETE, id) > 0;
    }

    private void insertEventTypeCosts(long classId, Map<EventType, BigDecimal> costs) {
        costs.forEach((eventType, cost) -> database.update(INSERT_EVENT_TYPE_COST, classId, eventType.name(), cost));
    }

    private Map<Long, Map<EventType, BigDecimal>> eventTypeCostsByClass() {
        Map<Long, Map<EventType, BigDecimal>> costs = new HashMap<>();
        for (EventTypeCost row : database.query(LIST_EVENT_TYPE_COSTS, ClassRepository::toEventTypeCost)) {
            costs.computeIfAbsent(row.classId(), id -> new EnumMap<>(EventType.class)).put(row.eventType(), row.cost());
        }
        return costs;
    }

    private static EventTypeCost toEventTypeCost(ResultSet row) throws SQLException {
        return new EventTypeCost(
                row.getLong("class_id"),
                EventType.valueOf(row.getString("event_type")),
                row.getBigDecimal("cost"));
    }

    private static PricedClass toPricedClass(ResultSet row) throws SQLException {
        return new PricedClass(
                row.getLong("id"),
                row.getString("name"),
                row.getString("description"),
                ClassType.valueOf(row.getString("class_type")),
                row.getBigDecimal("cost"));
    }

    private record EventTypeCost(long classId, EventType eventType, BigDecimal cost) {
    }
}
