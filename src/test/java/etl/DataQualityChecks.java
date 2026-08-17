package etl;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;
import utils.DatabaseConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Direct-database data quality checks against OpenEMR's MySQL schema --
 * the "Data & ETL Testing" leg of this framework's QA coverage, alongside
 * the UI suite (Selenium) and the API suite (RestAssured).
 *
 * WHY THIS IS SEPARATE FROM THE MAIN SUITES: the shared public demo
 * (demo.openemr.io) does not expose direct MySQL access -- nor should any
 * public demo. These checks are only meaningful against a self-hosted or
 * Dockerized OpenEMR instance you control. Rather than pretend that
 * limitation doesn't exist, every test here requires an explicit
 * -Ddb.enabled=true opt-in (see ConfigReader/DatabaseConnectionHelper) and
 * the whole class is skipped -- not failed -- with a specific, actionable
 * message when that flag isn't set or the database isn't reachable. See
 * docs/qa/Data-ETL-Testing.md for how to stand up a target instance.
 *
 * WHY THESE SPECIFIC CHECKS: they're the standard data-quality categories
 * from the job description this module exists to demonstrate -- null/
 * required-field checks, duplicate detection, referential integrity, and
 * source-to-target reconciliation -- applied to OpenEMR's actual core
 * tables (patient_data, form_encounter, lists) rather than a toy schema.
 * Table/column names are based on OpenEMR's documented core schema; if
 * your instance is a custom fork, adjust the SQL below to match.
 *
 * Run standalone: mvn -B test -DsuiteXmlFile=testng-data-quality.xml
 *   -Ddb.enabled=true -Ddb.host=... -Ddb.name=... -Ddb.user=... -Ddb.password=...
 */
public class DataQualityChecks {

    @BeforeClass
    public void verifyDatabaseReachable() {
        if (!ConfigReader.isDatabaseEnabled()) {
            throw new SkipException(
                    "Skipping Data/ETL checks: -Ddb.enabled=true was not set. These checks need direct "
                    + "MySQL access to an OpenEMR instance you control (not the shared public demo). "
                    + "See docs/qa/Data-ETL-Testing.md.");
        }
        if (!DatabaseConnectionHelper.isReachable()) {
            throw new SkipException(
                    "Skipping Data/ETL checks: could not reach " + ConfigReader.getDbJdbcUrl()
                    + " as '" + ConfigReader.getDbUser() + "'. See docs/qa/Data-ETL-Testing.md for setup "
                    + "and the -Ddb.* overrides.");
        }
    }

    // ---- Null / required-field checks ----

    @Test(description = "Every patient record has the demographic fields OpenEMR itself treats as mandatory")
    public void patientRecordsHaveRequiredFields() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patient_data "
                + "WHERE fname IS NULL OR fname = '' "
                + "   OR lname IS NULL OR lname = '' "
                + "   OR DOB IS NULL";
        long badRows = scalarCount(sql);
        Assert.assertEquals(badRows, 0,
                badRows + " patient_data row(s) are missing fname, lname, or DOB");
    }

    // ---- Duplicate detection ----

    @Test(description = "No two patients share the exact same first name, last name, and date of birth")
    public void noDuplicatePatientsByNameAndDob() throws SQLException {
        String sql = "SELECT fname, lname, DOB, COUNT(*) AS cnt "
                + "FROM patient_data "
                + "GROUP BY fname, lname, DOB "
                + "HAVING COUNT(*) > 1";
        try (Connection conn = DatabaseConnectionHelper.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            StringBuilder duplicates = new StringBuilder();
            int groups = 0;
            while (rs.next()) {
                groups++;
                duplicates.append(String.format("%s %s (DOB %s) x%d; ",
                        rs.getString("fname"), rs.getString("lname"), rs.getString("DOB"), rs.getInt("cnt")));
            }
            Assert.assertEquals(groups, 0, "Found duplicate patient records: " + duplicates);
        }
    }

    // ---- Referential integrity ----

    @Test(description = "Every encounter references a patient that actually exists (no orphaned encounters)")
    public void encountersReferenceExistingPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM form_encounter fe "
                + "LEFT JOIN patient_data pd ON fe.pid = pd.pid "
                + "WHERE pd.pid IS NULL";
        long orphaned = scalarCount(sql);
        Assert.assertEquals(orphaned, 0,
                orphaned + " form_encounter row(s) reference a pid with no matching patient_data record");
    }

    @Test(description = "Every problem/allergy/medication list entry references a patient that actually exists")
    public void listEntriesReferenceExistingPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM lists l "
                + "LEFT JOIN patient_data pd ON l.pid = pd.pid "
                + "WHERE pd.pid IS NULL";
        long orphaned = scalarCount(sql);
        Assert.assertEquals(orphaned, 0,
                orphaned + " lists row(s) (problems/allergies/medications) reference a pid with no matching patient");
    }

    // ---- Source-to-target reconciliation ----

    /**
     * Generic count-based reconciliation between two JDBC sources -- the
     * pattern used to verify an ETL load didn't drop or duplicate rows in
     * transit. Opt-in via a second connection (-Dreconcile.target.url,
     * -Dreconcile.target.user, -Dreconcile.target.password) since a single
     * self-hosted OpenEMR instance has no separate "target" system to
     * compare against by default; skips with a clear explanation otherwise
     * rather than being deleted, so the pattern is here to point at a real
     * warehouse/replica when one exists.
     */
    @Test(description = "Patient row count matches between source and target databases, when a target is configured")
    public void patientRowCountReconcilesSourceToTarget() throws SQLException {
        String targetUrl = System.getProperty("reconcile.target.url");
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new SkipException(
                    "Skipping source-to-target reconciliation: no -Dreconcile.target.url configured. "
                    + "This check compares patient_data row counts between two databases (e.g. an OpenEMR "
                    + "primary and a reporting/ETL replica) -- point it at a real target to exercise it.");
        }
        String targetUser = System.getProperty("reconcile.target.user", ConfigReader.getDbUser());
        String targetPassword = System.getProperty("reconcile.target.password", ConfigReader.getDbPassword());

        long sourceCount = scalarCount("SELECT COUNT(*) FROM patient_data");
        long targetCount;
        try (Connection targetConn = java.sql.DriverManager.getConnection(targetUrl, targetUser, targetPassword);
             Statement st = targetConn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patient_data")) {
            rs.next();
            targetCount = rs.getLong(1);
        }
        Assert.assertEquals(targetCount, sourceCount,
                "Source patient_data count (" + sourceCount + ") does not match target count (" + targetCount + ")");
    }

    // ---- Helper ----

    private long scalarCount(String sql) throws SQLException {
        try (Connection conn = DatabaseConnectionHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
