package unit;

import org.testng.annotations.Test;
import utils.TestDataGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestDataGeneratorTest {

    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void randomFirstNameIsNeverBlank() {
        assertFalse(TestDataGenerator.randomFirstName().trim().isEmpty());
    }

    @Test
    public void randomLastNameIsNeverBlank() {
        assertFalse(TestDataGenerator.randomLastName().trim().isEmpty());
    }

    @Test
    public void randomGenderIsOneOfTheSupportedValues() {
        String gender = TestDataGenerator.randomGender();
        assertTrue(gender.equals("Male") || gender.equals("Female") || gender.equals("Unspecified"));
    }

    @Test
    public void randomDobIsAParsablePastDate() {
        String dob = TestDataGenerator.randomDob();
        LocalDate parsed = LocalDate.parse(dob, DOB_FORMAT);
        assertTrue(parsed.isBefore(LocalDate.now()), "Generated DOB should be in the past");
    }

    @Test
    public void randomPatientPopulatesAllFields() {
        TestDataGenerator.Patient patient = TestDataGenerator.randomPatient();
        assertFalse(patient.firstName.trim().isEmpty());
        assertFalse(patient.lastName.trim().isEmpty());
        assertFalse(patient.dob.trim().isEmpty());
        assertFalse(patient.gender.trim().isEmpty());
        // Confirms the DOB is actually parsable in the format the patient form expects
        LocalDate.parse(patient.dob, DOB_FORMAT);
    }
}
