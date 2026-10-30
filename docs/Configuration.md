# Configuration

Configuration lives in `src/main/resources/application.yaml`. Every property can be overridden with an environment
variable using Spring's relaxed binding - uppercase, dots and dashes become underscores - for example
`sample-coupon-service.rest-client.read-timeout` becomes `SAMPLE_COUPON_SERVICE_REST_CLIENT_READ_TIMEOUT`.

## Properties

Defaults from `application.yaml`, plus the custom properties of the service:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sample_coupon
    username: postgres
    password: postgres
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis              # none disables caching

sample-coupon-service:
  geolocation:
    # URL template, {ipAddr} is replaced with the client IP. The default is a placeholder -
    # activate a profile (see below) or override it.
    endpoint: http://localhost:8080/geolocation/{ipAddr}
  rest-client:               # outgoing HTTP calls, i.e. geolocation
    connect-timeout: 300ms
    read-timeout: 1500ms
  caching:                   # see Geolocation Caching page
    ip-addr-geo-location-ttl: 8h
    ip-addr-geo-location-ttl-jitter: 1h
    local-ttl: 1m
    local-max-size: 10000
  clock:
    # application time zone: IANA id (Europe/Warsaw, UTC) or offset (+01:00, UTC+01:00)
    zone: UTC
```

Caching settings are explained in [Geolocation Caching](./Geolocation-Caching.md#configuration).

## Profiles

- **`prod`** - geolocation endpoint set to `http://ip-api.com/json/{ipAddr}`. Use in production.
- **`local`** - local development against [`localhost/`](../localhost) dependencies: geolocation endpoint set to
  the WireMock stub at `http://localhost:1080`, sample data seeded from `localhost/db/seed`. Default profile, active
  when no other profile is set.
- **`integrationTest`** - test-only. Disables caching and points geolocation at the test WireMock.

## Docker image

The image (`ghcr.io/damianmalczewski/sample-coupon-service`) accepts:

- **`LOGGING_STRUCTURED_FORMAT_CONSOLE`** - structured log format: `ecs` (default), `gelf` or `logstash`. Set to
  empty for plain text.
- **`JAVA_OPTS_EXTRA`** - extra JVM options, appended to the defaults (container-aware heap at 75% of RAM, exit and
  heap dump on out-of-memory).
- **`APP_ARGS`** - application arguments, for example `--spring.profiles.active=prod`.

Plus any property override as an environment variable, as above.
