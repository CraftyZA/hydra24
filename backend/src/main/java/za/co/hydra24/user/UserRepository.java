package za.co.hydra24.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.hydra24.db.Database;

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
    Database database;

    /** Creates the user on first sign-in, and stamps the sign-in time after that. */
    public void recordSignIn(UUID cognitoSub, String email) {
        database.update(RECORD_SIGN_IN, cognitoSub, email);
    }
}
