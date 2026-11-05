package za.co.hydra24.profile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.hydra24.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProfileRepository {

    private static final String FIND = """
            SELECT p.first_name, p.last_name, p.cellphone, p.date_of_birth, p.gender, p.preferred_class_id,
                   p.emergency_name, p.emergency_no, p.id_number, p.race, p.disability
              FROM profiles p
              JOIN users u ON u.id = p.user_id
             WHERE u.cognito_sub = ?
            """;

    private static final String FIND_ID = """
            SELECT p.id
              FROM profiles p
              JOIN users u ON u.id = p.user_id
             WHERE u.cognito_sub = ?
            """;

    private static final String SAVE = """
            INSERT INTO profiles (user_id, first_name, last_name, cellphone, date_of_birth, gender, preferred_class_id,
                                  emergency_name, emergency_no, id_number, race, disability)
            VALUES ((SELECT id FROM users WHERE cognito_sub = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE
               SET first_name = EXCLUDED.first_name,
                   last_name = EXCLUDED.last_name,
                   cellphone = EXCLUDED.cellphone,
                   date_of_birth = EXCLUDED.date_of_birth,
                   gender = EXCLUDED.gender,
                   preferred_class_id = EXCLUDED.preferred_class_id,
                   emergency_name = EXCLUDED.emergency_name,
                   emergency_no = EXCLUDED.emergency_no,
                   id_number = EXCLUDED.id_number,
                   race = EXCLUDED.race,
                   disability = EXCLUDED.disability,
                   updated_at = now()
            """;

    @Inject
    Database database;

    public Optional<Profile> find(UUID cognitoSub) {
        return database.queryOne(FIND, ProfileRepository::toProfile, cognitoSub);
    }

    public Optional<Long> findId(UUID cognitoSub) {
        return database.queryOne(FIND_ID, row -> row.getLong("id"), cognitoSub);
    }

    public void save(UUID cognitoSub, Profile profile) {
        database.update(SAVE, cognitoSub,
                profile.firstName(), profile.lastName(), profile.cellphone(), profile.dateOfBirth(),
                Database.enumName(profile.gender()), profile.preferredClassId(),
                profile.emergencyName(), profile.emergencyNo(), profile.idNumber(),
                Database.enumName(profile.race()), Database.enumName(profile.disability()));
    }

    private static Profile toProfile(ResultSet row) throws SQLException {
        return new Profile(
                row.getString("first_name"),
                row.getString("last_name"),
                row.getString("cellphone"),
                row.getObject("date_of_birth", LocalDate.class),
                Database.toEnum(Gender.class, row.getString("gender")),
                row.getObject("preferred_class_id", Long.class),
                row.getString("emergency_name"),
                row.getString("emergency_no"),
                row.getString("id_number"),
                Database.toEnum(Race.class, row.getString("race")),
                Database.toEnum(Disability.class, row.getString("disability")));
    }
}
