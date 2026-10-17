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

Run with the `wiremock` profile, which points the geolocation client at the local WireMock stub:

```bash
./gradlew bootRun --args='--spring.profiles.active=wiremock'
```

The application listens on `http://localhost:8080`.

## Hoppscotch collection

The [`sample-coupon-service.hoppscotch.json`](sample-coupon-service.hoppscotch.json) is
a [Hoppscotch](https://hoppscotch.io) collection export with predefined requests. Import it in Hoppscotch from the
collections panel, choosing the Hoppscotch import format.

Requests share state through environment variables, set by scripts executed pre or post queries.
