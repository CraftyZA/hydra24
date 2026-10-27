package za.co.hydra24.user;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

    // Keyed on the email (case-insensitive): a Cognito account deleted and
    // registered again gets a new sub but keeps its users row.
    private static final String RECORD_SIGN_IN = """
            INSERT INTO users (cognito_sub, email) VALUES (?, ?)
            ON CONFLICT ((lower(email))) DO UPDATE
               SET cognito_sub = EXCLUDED.cognito_sub,
                   email = EXCLUDED.email,
                   last_login_at = now()
            """;

    @Inject
    AgroalDataSource dataSource;

    /** Creates the user on first sign-in, and stamps the sign-in time after that. */
    public void recordSignIn(UUID cognitoSub, String email) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RECORD_SIGN_IN)) {
            statement.setObject(1, cognitoSub);
            statement.setString(2, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record sign-in", e);
        }
    }
}
