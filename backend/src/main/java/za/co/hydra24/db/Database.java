package za.co.hydra24.db;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Parameterised SQL with the JDBC boilerplate in one place. */
@ApplicationScoped
public class Database {

    private static final String FOREIGN_KEY_VIOLATION = "23503";

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet row) throws SQLException;
    }

    @Inject
    AgroalDataSource dataSource;

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rows = statement.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rows.next()) {
                results.add(mapper.map(rows));
            }
            return results;
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        return query(sql, mapper, params).stream().findFirst();
    }

    /** Runs an INSERT, UPDATE or DELETE and returns the number of rows changed. */
    public int update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = prepare(connection, sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    /** The enum's name for a TEXT column; null stays null. */
    public static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    /** The enum stored in a TEXT column; null stays null. */
    public static <E extends Enum<E>> E toEnum(Class<E> type, String name) {
        return name == null ? null : Enum.valueOf(type, name);
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private static RuntimeException translate(SQLException e) {
        if (FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
            return new ForeignKeyViolationException(e);
        }
        return new IllegalStateException("Database call failed", e);
    }
}
