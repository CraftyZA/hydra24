package za.co.hydra24.member;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.OffsetDateTime;

/**
 * A registered user, as the admins see them. memberNo and the name fields are null
 * until the user sets up a profile.
 */
@RegisterForReflection
public record Member(long userId, String email, Long memberNo, String firstName, String lastName,
                     String cellphone, OffsetDateTime joinedAt, OffsetDateTime lastLoginAt) {
}
