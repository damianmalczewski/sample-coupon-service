# Sample Coupon Service

A coupon service: operators create coupons limited to a country and a number of uses, and end users redeem them. The
service enforces the rules on redemption - the user must be in the coupon's country, the coupon must have uses left,
and each user can redeem a coupon only once - and stays correct when many users redeem the same coupon at the same
time.

It is a sandbox project for trying out ideas, built with Kotlin, Spring Boot, PostgreSQL and Redis.

> These pages live in the [`docs/`][docs-folder] folder of the repository and are published to the wiki on every
> push to `main`. Edit them there - changes made directly in the wiki are overwritten on the next publish.

## Contents

**Business**

- [Business Rules](./Business-Rules.md) - what coupons are, how redemption works, what gets rejected and why.
- [REST API](./REST-API.md) - endpoints, payloads, validation and error responses.

**Technical**

- [Architecture](./Architecture.md) - package layout, dependency rules, persistence, error handling, observability.
- [Redemption Concurrency](./Redemption-Concurrency.md) - how simultaneous redemptions of one coupon stay within its
  limit.
- [Geolocation Caching](./Geolocation-Caching.md) - two-level cache in front of the IP geolocation API.
- [Configuration](./Configuration.md) - properties, profiles and environment variables.
- [Development](./Development.md) - running locally, testing, CI, releases and these docs.

## At a glance

- **Runtime:** Kotlin on Java 25, virtual threads
- **Framework:** Spring Boot 4 - Web MVC, Data JPA, Data Redis, Validation
- **Storage:** PostgreSQL with Flyway migrations; Caffeine in front of Redis for caching
- **External service:** [ip-api.com](https://ip-api.com), client IP to country
- **Errors:** RFC 9457 Problem Details via [Problem4J](https://problem4j.github.io)
- **API docs:** OpenAPI, Swagger UI at `/swagger-ui/index.html`
- **Tests:** Kotest, ArchUnit, Testcontainers, WireMock
- **Delivery:** Docker image on `ghcr.io`, GitHub Actions for CI and releases

[docs-folder]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/docs
