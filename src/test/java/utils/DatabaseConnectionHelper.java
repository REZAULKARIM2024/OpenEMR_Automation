package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thin JDBC connection helper for the Data/ETL quality checks in
 * {@code etl.DataQualityChecks}.
 *
 * Deliberately minimal -- no connection pool, since these are batch-style
 * validation queries run once per suite execution, not a live application.
 * The point of this class is entirely the diagnostic message: "no database
 * configured" and "database configured but unreachable" are reported as two
 * different, specific outcomes, following the same philosophy as
 * ApiAuthHelper/PreflightHealthCheck elsewhere in this framework -- fail
 * with a message that tells you what to do next, not a raw stack trace.
 */
public final class DatabaseConnectionHelper {

    private DatabaseConnectionHelper() {
    }

    /**
     * @return a fresh JDBC connection to ConfigReader.getDbJdbcUrl().
     * @throws IllegalStateException if db.enabled=true was not set (an explicit opt-in, since
     *         attempting a DB connection against whatever ConfigReader's defaults resolve to
     *         would otherwise silently try -- and fail against -- localhost:3306 in every run).
     * @throws SQLException if the database could not be reached with the configured credentials.
     */
    public static Connection getConnection() throws SQLException {
        if (!ConfigReader.isDatabaseEnabled()) {
            throw new IllegalStateException(
                    "Data/ETL checks were run without -Ddb.enabled=true. This is required even if "
                    + "db.host/db.name/etc resolve to something reachable, so these checks never run "
                    + "silently against the wrong database. See docs/qa/Data-ETL-Testing.md for setup.");
        }
        try {
            return DriverManager.getConnection(
                    ConfigReader.getDbJdbcUrl(), ConfigReader.getDbUser(), ConfigReader.getDbPassword());
        } catch (SQLException e) {
            throw new SQLException(
                    "Could not connect to " + ConfigReader.getDbJdbcUrl() + " as '" + ConfigReader.getDbUser()
                    + "'. The shared public OpenEMR demo does not expose direct database access -- these "
                    + "checks are meant to run against a self-hosted/Dockerized instance you control. "
                    + "See docs/qa/Data-ETL-Testing.md for the docker-compose reference and the "
                    + "-Ddb.host/-Ddb.port/-Ddb.name/-Ddb.user/-Ddb.password overrides. Underlying error: "
                    + e.getMessage(), e);
        }
    }

    /** @return true if a connection can be opened right now; never throws. */
    public static boolean isReachable() {
        if (!ConfigReader.isDatabaseEnabled()) {
            return false;
        }
        try (Connection c = getConnection()) {
            return c.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
}
