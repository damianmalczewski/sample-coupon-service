# Compose

Standalone demo deployment - the service and its dependencies, all in containers, using the published Docker image.

For local development (dependencies only, application started from the IDE or Gradle), see
[`localhost/`](../localhost).

## Services

[`docker-compose.yaml`](docker-compose.yaml) defines:

| Service                 | Address          | Purpose                             |
|-------------------------|------------------|-------------------------------------|
| `sample-coupon-service` | `127.0.0.1:8080` | the application                     |
| `postgres`              | internal only    | primary database (`sample_coupon`)  |
| `redis`                 | internal only    | geolocation response cache          |
| `wiremock`              | internal only    | stub for the IP geolocation service |

Only the application is published on the host. PostgreSQL, Redis and WireMock are reachable only from inside the
compose networks. The application waits for PostgreSQL and Redis to become healthy before starting.

WireMock serves stubs from [`wiremock/mappings`](wiremock/mappings). The bundled stub resolves every IP address to
`PL`, so coupons created with `countryCode: PL` can be redeemed.

## Running

```bash
docker compose -f compose/docker-compose.yaml up -d
```

The image is `ghcr.io/damianmalczewski/sample-coupon-service:latest`. To run local changes instead of the published
image, build it first with [`dockerize.sh`](../dockerize.sh), which tags the local build with the same name:

```bash
./dockerize.sh
```

## Configuration

The application is configured via environment variables in the compose file:

- `SPRING_DATASOURCE_*` and `SPRING_DATA_REDIS_*` point at the compose services,
- `SAMPLE_COUPON_SERVICE_GEOLOCATION_ENDPOINT` points the geolocation client at WireMock,
- `LOGGING_STRUCTURED_FORMAT_CONSOLE` is set to empty, switching the image's default ECS JSON logs to plain text.

## Trying it out

The [Hoppscotch collection](../localhost/sample-coupon-service.hoppscotch.json) targets `http://localhost:8080`, so it
works against this setup as well. See [`localhost/README.md`](../localhost/README.md) for details.
