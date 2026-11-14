package za.co.hydra24.member;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.hydra24.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MemberRepository {

    private static final String SELECT = """
            SELECT u.id, u.email, p.id AS member_no, p.first_name, p.last_name, p.cellphone,
                   u.created_at, u.last_login_at
              FROM users u
              LEFT JOIN profiles p ON p.user_id = u.id
            """;

    private static final String LIST_ALL = SELECT + " ORDER BY p.last_name NULLS LAST, p.first_name, lower(u.email)";

    private static final String FIND = SELECT + " WHERE u.id = ?";

    @Inject
    Database database;

    public List<Member> listAll() {
        return database.query(LIST_ALL, MemberRepository::toMember);
    }

    public Optional<Member> find(long userId) {
        return database.queryOne(FIND, MemberRepository::toMember, userId);
    }

    private static Member toMember(ResultSet row) throws SQLException {
        return new Member(
                row.getLong("id"),
                row.getString("email"),
                row.getObject("member_no", Long.class),
                row.getString("first_name"),
                row.getString("last_name"),
                row.getString("cellphone"),
                row.getObject("created_at", OffsetDateTime.class),
                row.getObject("last_login_at", OffsetDateTime.class));
    }
}
