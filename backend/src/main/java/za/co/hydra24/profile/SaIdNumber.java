package za.co.hydra24.profile;

/** South African ID numbers: 13 digits, the last one a Luhn check digit. */
final class SaIdNumber {

    private static final int LENGTH = 13;

    private SaIdNumber() {
    }

    static boolean isValid(String number) {
        if (number == null || !number.matches("\\d{" + LENGTH + "}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < LENGTH; i++) {
            int digit = number.charAt(i) - '0';
            boolean doubled = i % 2 == 1;
            sum += doubled ? (digit * 2 > 9 ? digit * 2 - 9 : digit * 2) : digit;
        }
        return sum % 10 == 0;
    }
}
