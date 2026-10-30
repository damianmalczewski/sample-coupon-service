# Development

## Prerequisites

- JDK 25 (Gradle toolchains can provision it)
- Node.js 24 - only for the web UI, the Gradle build does not need it
- Docker - for local dependencies and for Testcontainers in integration tests

## Running locally

Two setups are provided in the repository:

- [`localhost/`][localhost-setup] - dependencies in containers (PostgreSQL, Redis, WireMock, Redis Insight),
  application started from the IDE or Gradle. Includes a [Hoppscotch](https://hoppscotch.io) collection with
  predefined requests.
  ```bash
  docker compose -f localhost/docker-compose.yaml up -d
  ./gradlew bootRun
  ```
- [`compose/`][compose-setup] - standalone demo, the application runs from its Docker image too.
  ```bash
  docker compose -f compose/docker-compose.yaml up -d
  ```

Both bundle a WireMock stub that resolves every IP address to `PL`, so coupons created with `countryCode: PL` can be
redeemed. The application listens on `http://localhost:8080`; `/` serves the [web UI][webapp] when built, otherwise
redirects to Swagger UI.

## Web UI

The [web UI][webapp] is built manually, outside of Gradle:

```bash
cd webapp
npm ci
npm run dev     # dev server on http://localhost:5173, proxies /api to :8080
npm run build   # builds into src/main/resources/static (git-ignored), served by the application
```

The Gradle build packages whatever is in `src/main/resources/static` at the time - nothing on a fresh checkout, so `/`
redirects to Swagger UI. CI builds the UI fresh before Gradle on every build, so its jars and released images include it.

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
- **`rebuild.yml`** - weekly (Saturday 12:00 UTC) and manually. Rebuilds released images on a fresh base image, one
  job per version: the highest patch of each `X.Y` line listed in `.github/utils/supported_versions` and
  `.github/utils/legacy_versions` (both optional, the newest line stands in for a missing `supported_versions`). The
  jar is reused from the released image, the tags stay the same - only the highest release gets `latest`.
- **`wiki.yml`** - on push to `main` touching `docs/`, and manually. Publishes `docs/` to the GitHub wiki.

Releases follow the cactus model: `main` holds the newest version line, older lines get patches on `release/X.Y.x`
branches. Image tags: `X.Y.Z`, `X.Y`, `X` (highest of its major only) and `latest` (from `main` only).

### Releasing

A release is never triggered by a push or a tag - it has to be started by hand:

1. Open the [Release workflow][release-workflow] page in GitHub Actions.
2. Click **Run workflow** and pick the branch to release from - `main` for the newest version line, or
   `release/X.Y.x` for a patch of an older one.
3. Choose which part of the version to bump - `patch`, `minor` or `major` (only `patch` on `release/X.Y.x`).
4. Click **Run workflow** to start it.

The workflow refuses to run on any other branch, on a commit that is already released, or when the computed tag
already exists.

Dependabot updates GitHub Actions and Gradle dependencies monthly (minor and patch versions only).

## Documentation

These pages are the `docs/` folder of the repository. On every push to `main` that changes `docs/`, the `wiki.yml`
workflow publishes them to the GitHub wiki using [`Andrew-Chen-Wang/github-wiki-action`][github-wiki-action]:

- `docs/README.md` becomes the wiki's `Home` page, `docs/_Sidebar.md` its sidebar and `docs/_Footer.md` its footer.
- Links written as `./Page-Name.md` work both in the repository and in the wiki - the action rewrites them to wiki
  links. Link to files outside `docs/` (source code, other READMEs) with absolute URLs to the `main` branch, e.g.
  `https://github.com/damianmalczewski/sample-coupon-service/blob/main/...` - a relative `../` link breaks in the
  wiki.
- The wiki is a copy: edit `docs/`, not the wiki. Changes made in the wiki are overwritten by the next publish.
- File names become page titles, with dashes shown as spaces. Keep `docs/` flat, the wiki has no folders.
- Diagrams use [Mermaid](https://mermaid.js.org), which GitHub renders both in the repository and in the wiki.

[localhost-setup]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/localhost
[compose-setup]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/compose
[webapp]: https://github.com/damianmalczewski/sample-coupon-service/tree/main/webapp
[github-wiki-action]: https://github.com/Andrew-Chen-Wang/github-wiki-action
[release-workflow]: https://github.com/damianmalczewski/sample-coupon-service/actions/workflows/release.yml
