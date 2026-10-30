# Sample Coupon Service

[![CI](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/ci.yml/badge.svg)](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/ci.yml)
[![Release](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/release.yml/badge.svg)](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/release.yml)
[![Version](https://img.shields.io/github/v/release/damianmalczewski/sample-coupon-service?logo=github&label=Version)](https://github.com/damianmalczewski/sample-coupon-service/releases/latest)
[![Codecov](https://codecov.io/gh/damianmalczewski/sample-coupon-service/branch/main/graph/badge.svg?token=R90DKC1C2M)](https://codecov.io/gh/damianmalczewski/sample-coupon-service)

A throwaway application, used as a sandbox for trying out ideas.

Full documentation - business rules, REST API, architecture, concurrency, caching, configuration and development -
lives in [`docs/`](docs) and is published to the [wiki](https://github.com/damianmalczewski/sample-coupon-service/wiki).

## What it does

- Create coupons with country restriction and usage limit.
- Redeem coupons - enforces country match (resolved from client IP), usage limit, and per-user uniqueness.
- Resolves race of coupon redemptions.
- Geolocation responses cached in Caffeine in front of Redis, protected against thundering herd - see
  [Geolocation Caching](docs/Geolocation-Caching.md).
- Flyway-managed PostgreSQL schema.

## Development

Useful Gradle commands:

```bash
# Format and build - runs both test suites, coverage and Spotless
./gradlew

# Run unit tests only
./gradlew test

# Run integration tests only (Testcontainers)
./gradlew integrationTest
```

For local development, build the [web UI](webapp) once by hand (needs Node.js). Its output in
`src/main/resources/static` is git-ignored and Gradle does not build it, so a fresh checkout has no UI - `/` then
redirects to Swagger UI. Rebuild after pulling UI changes.

```bash
cd webapp
npm ci
npm run build
```

## Poking at the design

A mix of vertical-slices, screaming and semi-clean architecture.

- _"a mix"_ meaning that it has no strict rules and the architecture was formed in order to explore different styles,
- _"semi"_ meaning JPA annotations are allowed in domain classes, since adding a mapping layer just for this project's
  size felt like busywork.

Most configurable values live in properties, so they can be externalized via env vars or JVM args later if this ever
needs it.

### Coupon Redemption

The main explored business feature in here is handling race conditions while redeeming a coupon.

Redemption uses an atomic `UPDATE` for row reservation:

```kotlin
@Modifying
@Query(
    """
      UPDATE Coupon c
      SET c.usageCount = c.usageCount + 1
      WHERE c.code = :code
        AND c.usageCount < c.usageLimit
      """
)
fun attemptToRedeemInternal(code: String): Int
```

Zero rows affected means coupon exhausted or at limit, so transaction rolled back. Otherwise, concurrent transactions
will wait upon reaching this method until the current one concludes.

## Dependencies

- **PostgreSQL** - primary database.
- **Redis** - geolocation response cache.
- **ip-api.com** - translates client IP to country. Can use WireMock on `localhost`.
- **Docker** - needed for local services and Testcontainers.

---

- [`localhost/`](localhost) - local development setup. Spins up all dependencies, including a pre-configured WireMock
  stub for the IP geolocation service, plus a Hoppscotch collection with predefined requests.
- [`compose/`](compose) - standalone demo deployment of the service together with its dependencies.
