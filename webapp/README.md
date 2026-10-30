# Web UI

Minimal [Vue](https://vuejs.org) single-page app for browsing, creating and redeeming coupons, built with
[Vite](https://vite.dev).

- `/` - coupon list with a form to create a new coupon on top.
- `/redeem` - coupon redemption for a given user id, with optional idempotency key (request id).

## Development

Start the application on `http://localhost:8080` first (see [`localhost/`](../localhost)), then:

```bash
cd webapp
npm ci
npm run dev
```

The dev server on `http://localhost:5173` hot-reloads changes and proxies `/api` to the application. Set `API_TARGET`
to proxy elsewhere, e.g. `API_TARGET=http://localhost:18080 npm run dev`.

`npm ci` installs exactly what `package-lock.json` pins and never changes it. Use `npm install <package>` or
`npm update` only to change dependencies, and commit the updated lock file.

## Build

The build is manual - Gradle does not run it:

```bash
cd webapp
npm run build
```

It lands in [`src/main/resources/static`](../src/main/resources/static) (git-ignored), so the application, and any jar
built afterwards, serves the UI on `/`. Paths matching no endpoint nor file (client-side routes like `/redeem`) resolve
to `index.html`. Without a build, `/` redirects to Swagger UI.

Release builds (CI called by the Release workflow) run `npm ci` and `npm run build` before Gradle, so released jars and
Docker images always carry a fresh UI. Other CI builds leave it out.
