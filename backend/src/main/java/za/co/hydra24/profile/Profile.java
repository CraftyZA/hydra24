package za.co.hydra24.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** A person's own details. idNumber, race and disability are optional. */
@RegisterForReflection
public record Profile(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Pattern(regexp = PHONE, message = PHONE_MESSAGE) String cellphone,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        Long preferredClassId,
        @NotBlank @Size(max = 100) String emergencyName,
        @NotNull @Pattern(regexp = PHONE, message = PHONE_MESSAGE) String emergencyNo,
        String idNumber,
        Race race,
        Disability disability) {

    private static final String PHONE = "\\+?[0-9 ]{9,20}";
    private static final String PHONE_MESSAGE = "must be a phone number";

    /** This profile without the ID number, race and disability, for showing to someone else. */
    public Profile withoutSpecialInformation() {
        return new Profile(firstName, lastName, cellphone, dateOfBirth, gender, preferredClassId,
                emergencyName, emergencyNo, null, null, null);
    }

    @JsonIgnore
    @AssertTrue(message = "must be a valid South African ID number")
    public boolean isIdNumberValid() {
        return idNumber == null || SaIdNumber.isValid(idNumber);
    }
}
