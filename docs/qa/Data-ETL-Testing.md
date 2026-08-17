# Data & ETL Testing

Direct-database data quality checks for OpenEMR's MySQL schema, in `etl/DataQualityChecks.java`.
This complements the UI suite (Selenium) and API suite (RestAssured) with the third leg of QA
coverage: verifying the data itself, not just the application behavior on top of it.

## Why this doesn't run against the shared public demo

`demo.openemr.io` is a third party's shared instance — it doesn't expose (and shouldn't expose)
direct MySQL access. These checks are only meaningful against an OpenEMR instance you actually
control: a local install, a self-hosted server, or (recommended for trying this out) OpenEMR's
own official Docker image.

Every check requires an explicit `-Ddb.enabled=true` opt-in on top of reachability, and the
whole class skips — cleanly, with a specific message — rather than failing when that flag isn't
set or the database can't be reached. See `etl/DataQualityChecks.java` and
`utils/DatabaseConnectionHelper.java`.

## Setting up a target instance (Docker)

OpenEMR publishes an official Docker Compose stack that includes MySQL:

```bash
mkdir openemr-docker && cd openemr-docker
curl -O https://raw.githubusercontent.com/openemr/openemr-devops/master/docker/openemr/docker-compose.yml
docker compose up -d
```

Once it's up, the MySQL container is reachable on `localhost:3306` (default database `openemr`,
default user/password documented in that compose file — check it, since OpenEMR has changed
these across releases). Give the stack a few minutes on first boot; OpenEMR runs its own schema
migrations before the app (and therefore a stable schema for these checks) is ready.

## Running the checks

```bash
mvn -B test -DsuiteXmlFile=testng-data-quality.xml \
  -Ddb.enabled=true \
  -Ddb.host=localhost \
  -Ddb.port=3306 \
  -Ddb.name=openemr \
  -Ddb.user=openemr \
  -Ddb.password=<your-password>
```

All five properties have defaults matching a typical local Docker setup (see
`ConfigReader.getDb*()`); only `db.enabled=true` is mandatory since it's the explicit opt-in.

## What's checked

| Category | Check | Table(s) |
|---|---|---|
| Required fields | Every patient has `fname`, `lname`, and `DOB` populated | `patient_data` |
| Duplicates | No two patients share the same first name, last name, and DOB | `patient_data` |
| Referential integrity | Every encounter references a patient that exists | `form_encounter` → `patient_data` |
| Referential integrity | Every problem/allergy/medication entry references a patient that exists | `lists` → `patient_data` |
| Source-to-target reconciliation | Patient row count matches between two databases (e.g. primary vs. a reporting replica) | configurable via `-Dreconcile.target.*` |

The table/column names above are OpenEMR's documented core schema. If you're running a custom
fork with a modified schema, adjust the SQL in `etl/DataQualityChecks.java` to match.

## Source-to-target reconciliation

`patientRowCountReconcilesSourceToTarget()` is written generically: point `-Ddb.*` at your
source and `-Dreconcile.target.url` / `-Dreconcile.target.user` / `-Dreconcile.target.password`
at a target (a reporting database, a replica, a warehouse landing table — whatever your actual
ETL pipeline loads into) and it compares `patient_data` row counts between the two. Without a
configured target it skips with an explanation rather than silently passing, since "no target
configured" and "counts match" are very different outcomes that should never look the same in a
report.

## Extending this module

Adding a new data-quality check is the same shape every time: write the SQL, wrap it in a
`@Test` method that either does a `scalarCount(...)` assertion or an `executeQuery` with a
readable failure message, and it inherits the class-level `@BeforeClass` skip/reachability
guard automatically. Natural next additions: date-range sanity checks (no DOB in the future,
no encounter date before the patient's DOB), orphaned `patient_insurance`/`patient_documents`
rows, and code-set validation (e.g. `lists.diagnosis` values that don't match a known ICD-10
pattern).
