# Development

## Prerequisites

- JDK 25 (Gradle toolchains can provision it)
- Docker - for local dependencies and for Testcontainers in integration tests

## Running locally

Two setups are provided in the repository:

- [`localhost/`][localhost-setup] - dependencies in containers (PostgreSQL, Redis, WireMock, Redis Insight),
  application started from the IDE or Gradle. Includes a [Hoppscotch](https://hoppscotch.io) collection with
  predefined requests.
  ```bash
  docker compose -f localhost/docker-compose.yaml up -d
  ./gradlew bootRun --args='--spring.profiles.active=wiremock'
  ```
- [`compose/`][compose-setup] - standalone demo, the application runs from its Docker image too.
  ```bash
  docker compose -f compose/docker-compose.yaml up -d
  ```

Both bundle a WireMock stub that resolves every IP address to `PL`, so coupons created with `countryCode: PL` can be
redeemed. The application listens on `http://localhost:8080`; `/` redirects to Swagger UI.

## Build and tests

```bash
./gradlew                   # default tasks: format, build, both test suites, coverage
./gradlew test              # unit tests
./gradlew integrationTest   # integration tests (needs Docker)
./gradlew spotlessApply     # format sources (ktfmt)
```

- **`test`** (`src/test/kotlin`) - unit tests: architecture rules (`ArchitectureSpec`), caching, normalization,
  geolocation client.
- **`integrationTest`** (`src/integrationTest/kotlin`) - the whole application against real PostgreSQL
  (Testcontainers), with WireMock standing in for geolocation: REST endpoints and use cases, including redemption
  concurrency.

Tests are written with Kotest `BehaviorSpec` (Given / When / Then). Notes:

- Integration specs share one Spring context (`@ApplicationTest`), with a controllable `TestClock` and caching disabled.
- `beforeEachGiven` (in `TestFixtures.kt`) resets data before each `Given` block - Kotest's `beforeEach` would run
  only before the leaf `Then`, after the arrange and act steps.
- Concurrency tests must not be `@Transactional`, see
  [Redemption Concurrency](./Redemption-Concurrency.md#testing).

Coverage of both suites is merged by Kover and uploaded to Codecov from CI.

## Conventions

- Formatting is enforced by Spotless (ktfmt); `./gradlew` formats before building, CI fails on unformatted code.
- Architecture rules are tests - a dependency in the wrong direction fails the build. See
  [Architecture](./Architecture.md#packages).
- Database changes go into a new Flyway migration under `src/main/resources/db/migration/<year>/<month>/`, named
  `V<yyyy.MM.dd_HH.mm.ss>__<description>.sql`. Never edit an applied migration.

## CI and releases

GitHub Actions workflows in `.github/workflows/`:

- **`ci.yml`** - on push and PR to `main`, weekly and manually. Builds and tests on Java 25, uploads coverage,
  auto-merges Dependabot PRs.
- **`release.yml`** - manual, choosing patch, minor or major. Computes the version, runs CI, pushes the Docker image to
  `ghcr.io`, tags `vX.Y.Z` and creates a GitHub release.
- **`wiki.yml`** - on push to `main` touching `docs/`, and manually. Publishes `docs/` to the GitHub wiki.

Releases follow the cactus model: `main` holds the newest version line, older lines get patches on `release/X.Y.x`
branches. Image tags: `X.Y.Z`, `X.Y`, `X` (highest of its major only) and `latest` (from `main` only).

Dependabot updates GitHub Actions and Gradle dependencies monthly (minor and patch versions only).

## Documentation

These pages are the `docs/` folder of the repository. On every push to `main` that changes `docs/`, the `wiki.yml`
workflow publishes them to the GitHub wiki using [`Andrew-Chen-Wang/github-wiki-action`][github-wiki-action]:

- `docs/README.md` becomes the wiki's `Home` page, `docs/_Sidebar.md` its sidebar.
- Links written as `./Page-Name.md` work both in the repository and in the wiki - the action rewrites them to wiki
  links. Link to files outside `docs/` (source code, other READMEs) with absolute URLs to the `main` branch, e.g.
  `https://github.com/damianmalczewski/sample-coupon-service/blob/main/...` - a relative `../` link breaks in the
  wiki.
- The wiki is a copy: edit `docs/`, not the wiki. Changes made in the wiki are overwritten by the next publish.
- File names become page titles, with dashes shown as spaces. Keep `docs/` flat, the wiki has no folders.
- Diagrams use [Mermaid](https://mermaid.js.org), which GitHub renders both in the repository and in the wiki.

[localhost-setup]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/localhost
[compose-setup]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/compose
[github-wiki-action]: https://github.com/Andrew-Chen-Wang/github-wiki-action
