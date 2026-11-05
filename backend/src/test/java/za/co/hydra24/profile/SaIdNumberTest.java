package za.co.hydra24.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaIdNumberTest {

    private static final String VALID = "8001015009087";

    @Test
    void acceptsANumberWithACorrectCheckDigit() {
        assertTrue(SaIdNumber.isValid(VALID));
    }

    @Test
    void refusesAWrongCheckDigit() {
        assertFalse(SaIdNumber.isValid("8001015009088"));
    }

    @Test
    void refusesTheWrongLength() {
        assertFalse(SaIdNumber.isValid("800101500908"));
    }

    @Test
    void refusesNonDigits() {
        assertFalse(SaIdNumber.isValid("80010150090A7"));
    }
}
