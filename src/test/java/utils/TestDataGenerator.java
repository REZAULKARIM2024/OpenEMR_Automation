package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Lightweight, dependency-free test data generator.
 *
 * This project has no owned database or seed scripts (see the "Database
 * Overview" / "Seed / Utility Scripts" sections of the README) -- the
 * OpenEMR demo instance manages its own data. This utility is the closest
 * equivalent: it produces randomized-but-valid patient demographics so
 * data-driven scenarios aren't limited to the same few hardcoded names,
 * without pulling in an external library like java-faker.
 */
public final class TestDataGenerator {

    private static final String[] FIRST_NAMES = {
            "Alice", "Brian", "Carmen", "David", "Elena", "Farid", "Grace", "Hiro",
            "Isabel", "Jordan", "Karim", "Lena", "Marco", "Nadia", "Omar", "Priya"
    };

    private static final String[] LAST_NAMES = {
            "Johnson", "Smith", "Diaz", "Nguyen", "Petrova", "Karim", "Blake",
            "Rahman", "Garcia", "Kim", "Ivanov", "Ahmed", "Rossi", "Tanaka"
    };

    // "Unassigned" -- confirmed via real inspected HTML from the live demo's
    // Sex dropdown (id="form_sex_identified"); this OpenEMR version has no
    // "Unspecified" option at all.
    private static final String[] GENDERS = {"Male", "Female", "Unassigned"};

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TestDataGenerator() {
    }

    public static String randomFirstName() {
        return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
    }

    public static String randomLastName() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

    public static String randomGender() {
        return GENDERS[RANDOM.nextInt(GENDERS.length)];
    }

    /**
     * Generates a valid, past date of birth (age between 1 and 90) formatted
     * as yyyy-MM-dd, matching the format the patient form expects.
     */
    public static String randomDob() {
        int yearsAgo = 1 + RANDOM.nextInt(90);
        int dayOfYear = 1 + RANDOM.nextInt(365);
        LocalDate dob = LocalDate.now().minusYears(yearsAgo).withDayOfYear(dayOfYear);
        return dob.format(DOB_FORMAT);
    }

    /** Convenience holder for a fully generated patient record. */
    public static final class Patient {
        public final String firstName;
        public final String lastName;
        public final String dob;
        public final String gender;

        public Patient(String firstName, String lastName, String dob, String gender) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.dob = dob;
            this.gender = gender;
        }
    }

    public static Patient randomPatient() {
        return new Patient(randomFirstName(), randomLastName(), randomDob(), randomGender());
    }
}
