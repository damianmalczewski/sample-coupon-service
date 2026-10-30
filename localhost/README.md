# Localhost

Local development setup - dependencies run in containers, the application runs from the IDE or Gradle.

For a standalone demo deployment with the application containerized as well, see [`compose/`](../compose).

## Dependencies

The [`docker-compose.yaml`](docker-compose.yaml) file spins up everything the application needs:

| Service         | Address           | Purpose                                     |
|-----------------|-------------------|---------------------------------------------|
| PostgreSQL      | `127.0.0.1:5432`  | primary database (`sample_coupon`)          |
| Redis           | `127.0.0.1:6379`  | geolocation response cache                  |
| WireMock        | `127.0.0.1:1080`  | stub for the IP geolocation service         |
| Redis Insight   | `127.0.0.1:5540`  | Redis browser UI                            |

```bash
docker compose -f localhost/docker-compose.yaml up -d
```

WireMock serves stubs from [`wiremock/mappings`](wiremock/mappings). The bundled stub resolves every IP address to
`PL`, so coupons created with `countryCode: PL` can be redeemed locally.

## Running the application

Run with the `local` profile (default when no profile is set), which points the geolocation client at the local
WireMock stub and seeds sample data:

```bash
./gradlew bootRun
```

The application listens on `http://localhost:8080`.

The [web UI](../webapp) is not there by default - its build output in `src/main/resources/static` is git-ignored and
Gradle does not build it, so `/` redirects to Swagger UI. Build it by hand (needs Node.js), worth doing once after
checkout and again after pulling UI changes:

```bash
cd webapp
npm ci
npm run build
```

For working on the UI itself, use the dev server instead - see [`webapp/`](../webapp).

## Seed data

The `local` profile loads sample coupons from [`db/seed`](db/seed) on startup. Seed scripts are Flyway repeatable
migrations (`R__*.sql`), resolved from the filesystem, so they never end up in the application jar. They run after all
versioned migrations, in name order, and again whenever their content changes. Prefix each with a four-digit sequence
number (`R__0001_seed_coupons.sql`, `R__0002_...`) to keep that order explicit. Keep them idempotent
(`ON CONFLICT DO NOTHING`) - rows already in the database are not updated. To start over, drop the volume with
`docker compose -f localhost/docker-compose.yaml down -v`.

## Hoppscotch collection

The [`sample-coupon-service.hoppscotch.json`](sample-coupon-service.hoppscotch.json) is
a [Hoppscotch](https://hoppscotch.io) collection export with predefined requests. Import it in Hoppscotch from the
collections panel, choosing the Hoppscotch import format.

Requests share state through environment variables, set by scripts executed pre or post queries.
