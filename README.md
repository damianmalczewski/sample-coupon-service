# Sample Coupon Service

[![CI](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/ci.yml/badge.svg)](https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/ci.yml)
[![Codecov](https://codecov.io/gh/damianmalczewski/sample-coupon-service/graph/badge.svg?token=R90DKC1C2M)](https://codecov.io/gh/damianmalczewski/sample-coupon-service)

A throwaway application, used as a sandbox for trying out ideas.

## What it does

- Create coupons with country restriction and usage limit.
- Redeem coupons - enforces country match (resolved from client IP), usage limit, and per-user uniqueness.
- Resolves race of coupon redemptions.
- Geolocation responses cached in Redis to cut down external API calls.
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

## Poking at the design

A mix of vertical-slices, screaming and semi-clean architecture.

- _"a mix"_ meaning that it has no strict rules and the architecture was formed in order to explore different styles,
- _"semi"_ meaning JPA annotations are allowed in domain classes, since adding a mapping layer just for this project's
  size felt like busywork.

Most configurable values live in properties, so they can be externalized via env vars or JVM args later if this ever
needs it.

### Coupon Redemption

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

0 rows affected means coupon exhausted or at limit, so transaction rolled back. Otherwise, no concurrent transactions
will wait upon reaching this method until current one concludes.

### Error handling

`ErrorType` interface implemented by `CommonError` and `CouponError` enums. `DomainException` hierarchy (extended by
`CouponException` and others) gives each error its own exception class for clean `catch` and propagation.

DB uniqueness errors are handled by checking which index triggered the exception.

Error responses use [Problem4J](https://problem4j.github.io), an opinionated library that extends Spring Boot's
exception handling with custom RFC Problem Details implementation.

### Scalability

Stateless app, cache externalized to Redis - horizontal scaling is straightforward, though it's never had to scale
beyond a laptop.

## Dependencies

- **PostgreSQL** - primary database.
- **Redis** - geolocation response cache.
- **ip-api.com** - translates client IP to country. Can use WireMock on `localhost`.
- **Docker** - needed for local services and Testcontainers.

The `localhost/` directory has a `docker-compose.yaml` that spins up all dependencies, including a pre-configured
WireMock stub for the IP geolocation service.
