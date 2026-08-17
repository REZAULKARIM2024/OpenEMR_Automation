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

**Prerequisite: Docker Desktop.** The `docker` command only works if Docker Desktop is installed
and running. On Windows: download and install it from
[docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/), which
also enables the WSL2 backend it needs — then open a **new** terminal (the one you had open
before installing won't pick up the updated PATH) and confirm with `docker --version` before
continuing.

*(An earlier version of this doc pointed at
`openemr-devops/docker/openemr/docker-compose.yml` — that path doesn't exist in the repo, and
even OpenEMR's own "easy dev" compose file assumes it's run from inside a full clone of the
`openemr/openemr` repo, so a bare `curl` of it doesn't work standalone.)*

**Already done for you:** `docker-compose.local-openemr.yml` at the repo root is this exact
compose file, ready to run — no copy-pasting needed:

```bash
docker compose -f docker-compose.local-openemr.yml up -d
```

Its contents (official `openemr/openemr` + `mariadb` images from Docker Hub), for reference:

```yaml
services:
  mysql:
    image: mariadb:11
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: openemr
      MYSQL_USER: openemr
      MYSQL_PASSWORD: openemr
    ports:
      - "3306:3306"
    volumes:
      - openemr_mysql:/var/lib/mysql

  openemr:
    image: openemr/openemr:latest
    restart: always
    ports:
      - "8300:80"
      - "9300:443"
    environment:
      MYSQL_HOST: mysql
      MYSQL_ROOT_PASS: root
      MYSQL_USER: openemr
      MYSQL_PASS: openemr
      MYSQL_DATABASE: openemr
      OE_USER: admin
      OE_PASS: pass
      # Auto-enable the Standard REST API and OAuth2 Password Grant on first
      # boot, so AuthenticatedApiRunner (see the REST API section of the
      # README) works against this instance without a manual Administration
      # > Config > Connectors step.
      OPENEMR_SETTING_rest_api: 1
      OPENEMR_SETTING_rest_system_scopes_api: 1
      OPENEMR_SETTING_oauth_password_grant: 3
    depends_on:
      - mysql
    volumes:
      - openemr_sites:/var/www/localhost/htdocs/openemr/sites

volumes:
  openemr_mysql:
  openemr_sites:
```

From the repo root (once Docker Desktop is installed and running):

```bash
docker compose -f docker-compose.local-openemr.yml up -d
```

First boot takes several minutes — OpenEMR runs its own installer and schema migrations before
the app (and therefore a stable schema for these checks) is ready. Watch progress with
`docker compose -f docker-compose.local-openemr.yml logs -f openemr` until you see it settle;
`docker compose -f docker-compose.local-openemr.yml ps` should show both containers as
`healthy`/`running`.

Once it's up:
- **App:** `https://localhost:9300/interface/login/login.php` (self-signed cert — your browser
  will warn, that's expected) — log in with `admin` / `pass`.
- **MySQL:** reachable on `localhost:3306`, database `openemr`, user `openemr`, password
  `openemr` (matches `ConfigReader`'s defaults below, so no `-Ddb.*` overrides are needed with
  this compose file as written).

To tear it down: `docker compose -f docker-compose.local-openemr.yml down` (add `-v` to also
delete the volumes and start fresh).

## Running the checks

With the compose file above, `ConfigReader`'s defaults (`localhost:3306`, database `openemr`,
user/password `openemr`/`openemr`) already match, so only the explicit opt-in flag is needed:

```bash
mvn -B test -DsuiteXmlFile=testng-data-quality.xml -Ddb.enabled=true
```

If your instance uses different values, override any of them individually:

```bash
mvn -B test -DsuiteXmlFile=testng-data-quality.xml \
  -Ddb.enabled=true -Ddb.host=localhost -Ddb.port=3306 -Ddb.name=openemr -Ddb.user=openemr -Ddb.password=<your-password>
```

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
